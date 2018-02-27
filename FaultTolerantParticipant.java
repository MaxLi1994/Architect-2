import MessagePackage.Message;

import java.util.function.Consumer;

/**
 * Description:
 * This class is in charge of adding fault tolerance capability to participants.
 * It manages all necessary activities for being a fault tolerant participant
 * including:
 * 1. register the participant
 * 2. send hear beats
 */
public class FaultTolerantParticipant {

    private ParticipantType participantType;
    private Consumer<Message> sendMessage;

    public FaultTolerantParticipant(ParticipantType type, Consumer<Message> sendMessage) {
        this.participantType = type;
        this.sendMessage = sendMessage;
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
