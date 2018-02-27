import MessagePackage.Message;

import java.util.List;
import java.util.function.Consumer;

/**
 * Description:
 * This class is a fault tolerance abstraction of MessageManager.
 * It provides reliable message channel services for its users along with the almost same APIs as MessageManager.
 * Notice that in a single process there is only one instance of MessageBus
 */
public class MessageBus {
    private static MessageBus instance = null;

    private String[] messageManagerIPs;


    private MessageBus() {
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
    public void init(String[] messageManagerIPs) {
        this.messageManagerIPs = messageManagerIPs;
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
    public void SendMessage(Message evt) throws Exception {

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
        return 1l;
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
        return null;
    }

    /***************************************************************************
     * Purpose: This method allows participants to get current available messages in a List
     *
     * Arguments: None.
     *
     * Returns: List of Messages
     *
     * Exceptions: Participant not registered, Get messages exception
     *
     ****************************************************************************/
    public List<Message> getAvailableMessages() throws Exception {
        return null;
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
    public void registerForMessageManagerFailureEvent(Consumer<String> callback) {

    }

}
