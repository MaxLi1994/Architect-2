import MessagePackage.Message;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class is in charge of fault tolerance management and communicating with MessageBus for ECSMontior
 */
public class MonitorManager {
    private Map<ParticipantType, List<Long>> participantMap;

    public MonitorManager() {

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
    public void registerForParticipantFailureEvent(ParticipantType type, Consumer<String> callback) {

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
    public void registerForMessageManagerFailureEvent(Consumer<String> callback) {

    }

    /***************************************************************************
     * Purpose: This method simply exposes the SendMessage of MessageBus to ECSMonitor
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
     * Purpose: This method makes the object start working
     *
     * Arguments: None.
     *
     * Returns: None.
     *
     * Exceptions: None.
     *
     ****************************************************************************/
    public void start() {

    }
}
