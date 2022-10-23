import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import models.*;
import org.jsoup.Jsoup;
import services.Courier;
import services.SendService;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.text.DateFormatter;
import java.io.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class CalendarReminderTask extends TimerTask {

    static {
        Courier.init(System.getenv("courier_bearer_secret"));
    }

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

    @Override
    public void run() {
        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Calendar client = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Construct the {@link Calendar.Events.List} request, but don't execute it yet.
            Calendar.Events.List request = client.events().list("primary");
            // Check from now on onwards
            request.setTimeMin(new DateTime(new Date(), TimeZone.getTimeZone("UTC")));


            // Retrieve the events, one page at a time.
            String pageToken = null;

            do {
                request.setPageToken(pageToken);

                Events events = request.execute();
                List<Event> items = events.getItems();
                for (Event event : items.stream().filter(getEventFilter()).collect(Collectors.toList())) {
                    System.out.println("Checking attendance for event: " + event.getSummary());

                    for (EventAttendee attendee : event.getAttendees()) {

                        // Don't remind optional attendees
                        if (attendee.isOptional()) {
                            continue;
                        }

                        // Only call for action when action is needed.
                        if (! "needsAction".equals(attendee.getResponseStatus())) {
                            continue;
                        }

                        System.out.println("Sending call-to-action to: " + attendee.getEmail());
                        sendCallToAction(event, attendee);
                    }
                }

                pageToken = events.getNextPageToken();
            } while (pageToken != null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendCallToAction(Event event, EventAttendee attendee) {
        // Construct request.
        String email = attendee.getEmail();

        SendEnhancedRequestBody sendRequest = new SendEnhancedRequestBody();
        SendRequestMessage message = new SendRequestMessage();

        HashMap<String, String> to = new HashMap<>();
        to.put("email", email);
        message.setTo(to);

        sendRequest.setMessage(message);
        message.setTemplate(Configuration.TEMPLATE_ID);

        HashMap<String, Object> data = new HashMap<>();
        // https://developers.google.com/calendar/api/v3/reference/events
        HashMap<String, Object> googleEventData = new HashMap<>(event);
        data.put("formattedDateMessage", getFormattedDateString(event));
        data.put("formattedDescription", getFormattedDescription(event.getDescription()));
        message.setData(data);

        data.put("google", googleEventData);

        // Send
        try {
            new SendService().sendEnhancedMessage(sendRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Object getFormattedDescription(String description) {
        return Jsoup.parse(description).text().replace(Configuration.COURIER_TAG, "").trim();
    }

    private static String getFormattedDateString(Event event) {
        DateTimeFormatter hours = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter date = DateTimeFormatter.ofPattern("dd MMM");

        ZonedDateTime startTime = parseRfc3339(event.getStart().getDateTime().toStringRfc3339());
        ZonedDateTime endTime = parseRfc3339(event.getEnd().getDateTime().toStringRfc3339());

        String start = startTime.format(hours);
        String end = endTime.format(hours) + " " + endTime.format(date);

        if (startTime.getDayOfYear() != endTime.getDayOfYear()) {
            start += " " + startTime.format(date);
        }

        return String.format("This event will take place from %s to %s", start, end);
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        // Load client secrets.
        InputStream in = CalendarReminderTask.class.getResourceAsStream(Configuration.GOOGLE_CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + Configuration.GOOGLE_CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private Predicate<Event> getEventFilter() {
        List<Predicate<Event>> eventFilters = new ArrayList<>();

        // Only send reminders for tagged events.
        Predicate<Event> containsTag = event -> event.getDescription() != null && event.getDescription().toLowerCase().contains(Configuration.COURIER_TAG.toLowerCase());
        eventFilters.add(containsTag);

        // Only send reminders after the configured period has past after event creation.
        Instant thresholdDate = Instant.now().minus(Configuration.SEND_REMINDERS_AFTER);
        Predicate<Event> thresholdDatePassed = event -> Instant.parse(event.getCreated().toStringRfc3339()).isBefore(thresholdDate);
        eventFilters.add(thresholdDatePassed);

        return eventFilters.stream().reduce(Predicate::and).get();
    }

    // Credit: https://gist.github.com/Megaprog/0cea7e5ba15b475e840b0de519b90fbe
    private static final DateTimeFormatter rfc3339Formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX")
            .withResolverStyle(ResolverStyle.LENIENT);

    private static ZonedDateTime parseRfc3339(String rfcDateTime) {
        return ZonedDateTime.parse(rfcDateTime, rfc3339Formatter);
    }
}