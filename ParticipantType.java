/**
 * Description: This enum class defines all types of participant in this system.
 */
public enum ParticipantType {
    HUMIDITY_SENSOR("humidity_sensor"),
    HUMIDITY_CONTROLLER("humidity_controller"),
    TEMPERATURE_SENSOR("temperature_sensor"),
    TEMPERATURE_CONTROLLER("temperature_controller");

    private String text;

    ParticipantType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
