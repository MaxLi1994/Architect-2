import MessagePackage.Message;
import MessagePackage.MessageManagerInterface;
import MessagePackage.MessageQueue;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * Description:
 * This class is a fault tolerance abstraction of MessageManager.
 * It provides reliable message channel services for its users along with the almost same APIs as MessageManager.
 * Notice that in a single process there is only one instance of MessageBus
 */
public class MessageBus {
    private static MessageBus instance = null;
    private static final int MAX_CACHE_MESSAGE_COUNT = 1000;
    private static final String LOCAL_HOST = "localhost";

    private List<String> messageManagerIPs;
    private List<MessageManagerInterface> mmiList;
    private List<BiConsumer<String, Boolean>> messageManagerFailureCallbacks;
    private int mainChannelIndex = 0;
    private boolean[] livingChannels;
    private List<List<Message>> cacheMessageList;

    private MessageBus() {
        messageManagerIPs = new ArrayList<>();
        mmiList = new ArrayList<>();
        messageManagerFailureCallbacks = new ArrayList<>();
        cacheMessageList = new ArrayList<>();
    }

    /***************************************************************************
     * Purpose: Users of this class get the singleton by this method
     *
     * Arguments: None.
     *
     * Returns: MessageBus.
     *
     ****************************************************************************/
    public static MessageBus getInstance() {
        if (instance == null) {
            instance = new MessageBus();
        }

        return instance;
    }

    /***************************************************************************
     * Purpose:
     * Configurate all IP addresses of Message Managers in the network
     * and register to all message channels
     *
     * Arguments: String array - message manager IPs.
     *
     * Returns: None.
     *
     * Exceptions: register exception
     *
     ****************************************************************************/
    public void init(String[] messageManagerIPs) throws Exception {
        // detect duplicate IP address
        Set<String> ipSet = new HashSet<>(Arrays.asList(messageManagerIPs));
        if (ipSet.size() < messageManagerIPs.length) {
            throw new Exception("duplicate IP address found!");
        }
        // detect invalid IP address
        for (String s : messageManagerIPs) {
            if (LOCAL_HOST.equals(s)) continue;
            if (!validateIPAddress(s)) throw new Exception("invalid IP address: " + s);
        }

        livingChannels = new boolean[messageManagerIPs.length];
        Arrays.fill(livingChannels, true);

        for (String s : messageManagerIPs) {
            MessageManagerInterface mmi;
            if (LOCAL_HOST.equals(s)) {
                mmi = new MessageManagerInterface();
            } else {
                mmi = new MessageManagerInterface(s);
            }
            this.mmiList.add(mmi);
            this.messageManagerIPs.add(s);
            this.cacheMessageList.add(new LinkedList<>());
        }
    }

    /***************************************************************************
     * CONCRETE METHOD:: SendMessage
     * Purpose: This method sends an message to the message manager.
     *
     * Arguments: Message object.
     *
     * Returns: None.
     *
     * Exceptions: Participant not registered, Send message exception
     *
     ****************************************************************************/
    synchronized public void SendMessage(Message evt) throws Exception {
        if (defender()) return;

        for (int i = mainChannelIndex; i < mmiList.size(); i++) {
            if (!livingChannels[i]) continue;
            try {
                mmiList.get(i).SendMessage(evt);
            } catch (Exception e) {
                failSafe();
            }
        }
    }

    /***************************************************************************
     * CONCRETE METHOD:: UnRegister
     * Purpose: This method is called when the object is no longer used. Essentially
     * this method unregisters participants from the message manager. It is important
     * that participants actively unregister with the message manager. Failure to do
     * so will cause unconnected queues to fill up with messages over time. This
     * will result in a memory leak and eventual failure of the message manager.
     *
     * Arguments: long interger - the participants id.
     *
     * Returns: None.
     *
     * Exceptions: Participant not registered, unregister exception
     *
     ****************************************************************************/
    public void UnRegister(long id) throws Exception {
        if (defender()) return;

        mmiList.get(mainChannelIndex).UnRegister(id);
    }

    /***************************************************************************
     * CONCRETE METHOD:: UnRegister
     * Purpose: This is the special version of UnRegister(long id) which unregister
     * the caller itself from all message channels
     *
     * Arguments: None
     *
     * Returns: None.
     *
     * Exceptions: Participant not registered, unregister exception
     *
     ****************************************************************************/
    public void UnRegister() throws Exception {
        if (defender()) return;

        for (int i = mainChannelIndex; i < mmiList.size(); i++) {
            mmiList.get(i).UnRegister();
        }
    }


    /***************************************************************************
     * CONCRETE METHOD:: GetMyId
     * Purpose: This method allows participants to get their participant Id.
     *
     * Arguments: None.
     *
     * Returns: long integer - the participants id
     *
     * Exceptions: Participant not registered
     *
     ****************************************************************************/
    public long GetMyId() throws Exception {
        if(defender()) return -1l;

        return mmiList.get(mainChannelIndex).GetMyId();
    }

    /***************************************************************************
     * CONCRETE METHOD:: GetRegistrationTime
     * Purpose: This method allows participants to obtain the time of registration.
     *
     * Arguments: None.
     *
     * Returns: String time stamp in the format: yyyy MM dd::hh:mm:ss:SSS
     *											yyyy = year
     *											MM = month
     *											dd = day
     *											hh = hour
     *											mm = minutes
     *											ss = seconds
     *											SSS = milliseconds
     *
     * Exceptions: Participant not registered
     *
     ****************************************************************************/
    public String GetRegistrationTime() throws Exception {
        if(defender()) return null;

        return mmiList.get(mainChannelIndex).GetRegistrationTime();
    }

    /***************************************************************************
     * Purpose: This method allows participants to get current available messages in a List
     * This method also pull messages from standby message channels and cache certain amount
     * of messages for each message channel.
     *
     * Arguments: None.
     *
     * Returns: List of Messages
     *
     * Exceptions: Participant not registered, Get messages exception
     *
     ****************************************************************************/
    public List<Message> getAvailableMessages() throws Exception {
        if(defender()) return new ArrayList<>();

        List<Message> result = new LinkedList<>();

        MessageQueue mq = null;
        try {
            mq = mmiList.get(mainChannelIndex).GetMessageQueue();
        }catch (Exception e) {
            failSafe();
        }

        if (cacheMessageList.get(mainChannelIndex).size() != 0) {
            result.addAll(cacheMessageList.get(mainChannelIndex));
            cacheMessageList.get(mainChannelIndex).clear();
        }

        if(mq != null) {
            int size = mq.GetSize();
            for (int i = 0; i < size; i++) {
                result.add(mq.GetMessage());
            }
        }

        pullStandByMessages();

        return result;
    }

    private void pullStandByMessages() {

        for (int i = mainChannelIndex + 1; i < mmiList.size(); i++) {
            if (!livingChannels[i]) continue;

            MessageQueue mq;
            try {
                mq = mmiList.get(i).GetMessageQueue();
            } catch (Exception e) {
                failSafe(i);
                continue;
            }

            int size = mq.GetSize();
            List<Message> cmList = cacheMessageList.get(i);
            for (int j = 0; j < size; j++) {
                cmList.add(mq.GetMessage());
                if (cmList.size() > MAX_CACHE_MESSAGE_COUNT) cmList.remove(0);
            }
        }
    }

    /***************************************************************************
     * Purpose: This method allows participants to listen to message manager failure events.
     *
     * Arguments: Consumer<String> - callback function with the IP address of the failed message manager as input.
     *
     * Returns: None.
     *
     * Exceptions: None.
     *
     ****************************************************************************/
    public void registerForMessageManagerFailureEvent(BiConsumer<String, Boolean> callback) {
        messageManagerFailureCallbacks.add(callback);
    }

    /**
     * Fail safe method
     * When one of the channels fails, calling this method will:
     * 1. switch the main channel to a backup if the main channel fails
     * 2. notify failure event to observers
     */
    private void failSafe(int channelIndex) {
        if (channelIndex >= mmiList.size() || !livingChannels[channelIndex]) return;

        String IP = messageManagerIPs.get(channelIndex);
        livingChannels[channelIndex] = false;

        boolean foundBackup = false;
        for (int i = 0; i < mmiList.size(); i++) {
            if (livingChannels[i]) {
                foundBackup = true;
                mainChannelIndex = i;
                break;
            }
        }

        boolean finalFoundBackup = foundBackup;
        messageManagerFailureCallbacks.forEach(o -> o.accept(IP, !finalFoundBackup));
    }

    private void failSafe() {
        failSafe(mainChannelIndex);
    }

    private boolean defender() {
        if (mainChannelIndex >= mmiList.size()) return true;
        return false;
    }

    private boolean validateIPAddress(String ip) {
        return Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$").matcher(ip).matches();
    }
}
