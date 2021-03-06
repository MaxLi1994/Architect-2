import MessagePackage.Message;

import java.util.function.Consumer;

/**
 * Utility class for participants
 */
public class ParticipantUtility {

    /**
     * Construct a sendMessage lambda expression
     * handle exceptions of the original SendMessage method
     * @param em MessageManagerInterface
     * @return Consumer<Message>
     */
    public static Consumer<Message> sendMessageWrapper(MessageBus em) {
        return (Message msg) -> {
            try {
                em.SendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}
