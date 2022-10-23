import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String... args) {
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new CalendarReminderTask(), 0, Configuration.RUN_INTERVAL_MINUTES, TimeUnit.MINUTES);

        System.out.println("Started scheduler service");
    }
}
