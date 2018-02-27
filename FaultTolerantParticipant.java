import MessagePackage.Message;

import java.util.function.Consumer;

/**
 * Description:
 * This class is in charge of adding fault tolerance capability to participants.
 * It manages all necessary activities for being a fault tolerant participant
 * including:
 * 1. register the participant
 * 2. send heart beats
 */
public class FaultTolerantParticipant {

    private static final int HEART_BEAT_INTERVAL = 2500; // heart beat interval time

    private ParticipantType participantType; // participant type of caller
    private Consumer<Message> sendMessage; // SendMessage method from caller

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
     * Exceptions: Exception.
     *
     ****************************************************************************/
    public void start() throws Exception {
        register();

        HeartBeat hb = new HeartBeat();
        Thread t = new Thread(hb);
        t.start();
    }

    /**
     * Send register message
     */
    private void register() {
        Message registerMsg = new Message(MessageType.FAULT_TOLERANT_PARTICIPANT_REGISTER, this.participantType.toString());
        this.sendMessage.accept(registerMsg);
    }

    /**
     * Heart beat thread
     */
    private class HeartBeat implements Runnable {

        @Override
        public void run() {
            while (true) {
                Message heartBeat = new Message(MessageType.FAULT_TOLERANT_PARTICIPANT_HEART_BEAT);
                sendMessage.accept(heartBeat);

                try {
                    Thread.sleep(HEART_BEAT_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
