import MessagePackage.Message;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * This class is in charge of fault tolerance management and communicating with MessageBus for ECSMontior
 */
public class MonitorManager {
    private static final int PULL_MESSAGE_INTERVAL = 1000;
    private static final int HEART_BEAT_EXPIRE_TIME = 3000;

    private Map<ParticipantType, Set<Long>> participantMap;
    private Map<ParticipantType, Long> mainParticipantMap;
    private Map<Long, Long> participantLastHeartBeatMap;

    private Map<ParticipantType, List<Runnable>> participantReadyCallbacks;
    private Map<ParticipantType, Boolean> participantReadyEventFlag;
    private Map<ParticipantType, List<BiConsumer<Long, Boolean>>> participantFailureCallbacks;
    private List<Consumer<List<Message>>> incomingMessagesCallbacks;
    private List<Runnable> messageManagerReadyCallbacks;
    private List<BiConsumer<String, Boolean>> messageManagerFailureCallbacks;

    private MessageBus mb;

    public MonitorManager() {
        participantReadyCallbacks = new HashMap<>();
        participantFailureCallbacks = new HashMap<>();
        incomingMessagesCallbacks = new ArrayList<>();
        messageManagerReadyCallbacks = new ArrayList<>();
        messageManagerFailureCallbacks = new ArrayList<>();

        participantMap = new HashMap<>();
        participantReadyEventFlag = new HashMap<>();
        participantMap.put(ParticipantType.HUMIDITY_CONTROLLER, new HashSet<>());
        participantReadyEventFlag.put(ParticipantType.HUMIDITY_CONTROLLER, false);
        participantMap.put(ParticipantType.HUMIDITY_SENSOR, new HashSet<>());
        participantReadyEventFlag.put(ParticipantType.HUMIDITY_SENSOR, false);
        participantMap.put(ParticipantType.TEMPERATURE_CONTROLLER, new HashSet<>());
        participantReadyEventFlag.put(ParticipantType.TEMPERATURE_CONTROLLER, false);
        participantMap.put(ParticipantType.TEMPERATURE_SENSOR, new HashSet<>());
        participantReadyEventFlag.put(ParticipantType.TEMPERATURE_SENSOR, false);

        participantLastHeartBeatMap = new HashMap<>();
        mainParticipantMap = new HashMap<>();

        mb = MessageBus.getInstance();
    }

    /***************************************************************************
     * Purpose: This method allows ECSMonitor to listen to a particular participant ready event.
     *
     * Arguments:
     * ParticipantType - participant type
     * Runnable - callback
     *
     * Returns: None.
     *
     * Exceptions: None.
     *
     ****************************************************************************/
    public void registerForParticipantReadyEvent(ParticipantType type, Runnable callback) {
        if (!participantReadyCallbacks.containsKey(type)) participantReadyCallbacks.put(type, new ArrayList<>());
        participantReadyCallbacks.get(type).add(callback);
    }

    /***************************************************************************
     * Purpose: This method allows ECSMonitor to listen to a particular participant failure event.
     *
     * Arguments:
     * ParticipantType - participant type
     * Consumer<String> - callback with the failed participant id as input
     *
     * Returns: None.
     *
     * Exceptions: None.
     *
     ****************************************************************************/
    public void registerForParticipantFailureEvent(ParticipantType type, BiConsumer<Long, Boolean> callback) {
        if (!participantFailureCallbacks.containsKey(type)) participantFailureCallbacks.put(type, new ArrayList<>());
        participantFailureCallbacks.get(type).add(callback);
    }

    /***************************************************************************
     * Purpose: This method allows ECSMonitor to listen to the messages interested.
     *
     * Arguments:
     * Consumer<List<Message>> - callback with the message list as input
     *
     * Returns: None.
     *
     * Exceptions: None.
     *
     ****************************************************************************/
    public void registerForIncomingMessage(Consumer<List<Message>> callback) {
        incomingMessagesCallbacks.add(callback);
    }

    /***************************************************************************
     * Purpose: This method allows ECSMonitor to listen to the message manager ready event.
     *
     * Arguments:
     * Runnable - callback
     *
     * Returns: None.
     *
     * Exceptions: None.
     *
     ****************************************************************************/
    public void registerForMessageManagerReadyEvent(Runnable callback) {
        messageManagerReadyCallbacks.add(callback);
    }

    /***************************************************************************
     * Purpose: This method allows ECSMonitor to listen to the message manager failure event.
     *
     * Arguments:
     * Consumer<String> - callback with the failed message manager's IP address as input
     *
     * Returns: None.
     *
     * Exceptions: None.
     *
     ****************************************************************************/
    public void registerForMessageManagerFailureEvent(BiConsumer<String, Boolean> callback) {
        messageManagerFailureCallbacks.add(callback);
    }

    /***************************************************************************
     * Purpose: The following methods simply exposes the same methods of MessageBus to ECSMonitor
     *
     ****************************************************************************/
    public void SendMessage(Message evt) throws Exception {
        mb.SendMessage(evt);
    }

    public void UnRegister() throws Exception {
        mb.UnRegister();
    }

    public long GetMyId() throws Exception {
        return mb.GetMyId();
    }

    public String GetRegistrationTime() throws Exception {
        return mb.GetRegistrationTime();
    }


    /***************************************************************************
     * Purpose: This method makes the object start working
     *
     * Arguments: None.
     *
     * Returns: None.
     *
     * Exceptions: Exception.
     *
     ****************************************************************************/
    public void start(String[] IPAddresses) throws Exception {
        mb.init(IPAddresses);

        messageManagerReadyCallbacks.forEach(o -> new Thread(o).start());

        mb.registerForMessageManagerFailureEvent(this::messageManagerFailed);

        // pulling messages loop
        while (true) {

            List<Message> messageList = mb.getAvailableMessages();
            List<Message> filteredMessageList = new ArrayList<>();

            // message processing loop
            for (Message m : messageList) {

                if (m.GetMessageId() == MessageType.FAULT_TOLERANT_PARTICIPANT_HEART_BEAT) {
                    long participantID = m.GetSenderId();
                    ParticipantType type = ParticipantType.toPartipantType(m.GetMessage());

                    // add newcomer participant
                    if (type != null) {
                        Set<Long> participantSet = participantMap.get(type);
                        if (!participantSet.contains(participantID)) {
                            participantSet.add(participantID);
                        }
                        if (!mainParticipantMap.containsKey(type)) {
                            mainParticipantMap.put(type, participantID);
                        }
                    }

                    // refresh heart beat
                    participantLastHeartBeatMap.put(participantID, System.currentTimeMillis());
                } else {
                    if (isMainParticipant(m.GetSenderId())) {
                        filteredMessageList.add(m);
                    }
                }
            }

            // pass filtered messages to whoever cares
            incomingMessagesCallbacks.forEach(o -> o.accept(filteredMessageList));

            // detect ready state of participants
            Iterator<ParticipantType> it = participantMap.keySet().iterator();
            while (it.hasNext()) {
                ParticipantType type = it.next();
                if (!participantReadyEventFlag.get(type) && participantMap.get(type).size() > 0) {
                    participantReadyCallbacks.get(type).forEach(o -> new Thread(o).start());
                    participantReadyEventFlag.put(type, true);
                }
            }

            // detect liveness of participants
            it = participantMap.keySet().iterator();
            long currentTime = System.currentTimeMillis();
            while (it.hasNext()) {
                ParticipantType type = it.next();
                Set<Long> set = participantMap.get(type);
                Long[] setArray = set.stream().toArray(Long[]::new);
                for (int i = 0; i < setArray.length; i++) {
                    long id = setArray[i];
                    if (currentTime - participantLastHeartBeatMap.get(id) > HEART_BEAT_EXPIRE_TIME) { // participant died
                        // notify monitor
                        participantFailureCallbacks.get(type).forEach(o -> o.accept(id, set.size() <= 1));

                        // unregister from message channel
                        mb.UnRegister(id);

                        // clear from local variables
                        participantLastHeartBeatMap.remove(id);
                        set.remove(id);

                        // switch main participant
                        if (mainParticipantMap.get(type) == id && i < setArray.length - 1) {
                            mainParticipantMap.put(type, setArray[i + 1]);
                        }
                    }
                }
            }

            try {
                Thread.sleep(PULL_MESSAGE_INTERVAL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle message manager fail event
     *
     * @param IP
     * @param allFailed
     */
    private void messageManagerFailed(String IP, Boolean allFailed) {
        // reset
        participantMap.clear();
        mainParticipantMap.clear();
        participantLastHeartBeatMap.clear();

        // notify
        messageManagerFailureCallbacks.forEach(o -> o.accept(IP, allFailed));
    }

    private boolean isMainParticipant(long id) {
        Iterator<ParticipantType> it = mainParticipantMap.keySet().iterator();
        while (it.hasNext()) {
            long mainID = mainParticipantMap.get(it.next());
            if (mainID == id) {
                return true;
            }
        }
        return false;
    }

}
