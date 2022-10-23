import java.time.Duration;

public class Configuration {

    private Configuration() {
        // Constants
    }

    public static final long RUN_INTERVAL_MINUTES = 30;
    public static final String COURIER_TAG = "#Courier:RemindAttendees";
    public static final String TEMPLATE_ID = "1WYY4ZXQN04YR9MNETS553A0YMF6";
    public static final String GOOGLE_CREDENTIALS_FILE_PATH = "/credentials.json";
    public static final Duration SEND_REMINDERS_AFTER = Duration.ofHours(1);
}
