### About

This application demos the possibilities of an integration between [Courier](https://www.courier.com/) and third party apps, such as Google Calendar.
Specifically, this repo contains code that will send RSVPs to all invitees in all Calendar events tagged with `#Courier:RemindAttendees` in the description.

### Getting started

1. Setup Google Calendar API and add generated credentials to /resources/<credentialFile>.json
https://developers.google.com/calendar/api/quickstart/java
2. Create a [Courier template](https://www.courier.com/docs/guides/tutorials/how-to-design-a-notification-template/), and add the ID to `Configuration.java`.
In this template you can use fields from [Google Calendar Events](https://developers.google.com/calendar/api/v3/reference/events), or add your own in `CalenderReminderTask#sendCallToAction`.
3. Tag an event with `#Courier:RemindAttendees`. Note: this event must be older than the threshold configured in the Configuration.
4. Setup an email provider in Courier. [Docs](https://www.courier.com/docs/guides/tutorials/how-to-add-an-integration-to-a-channel/)
5. Deploy this code on your server, or simply run from your IDE.
6. Enjoy people not responding to events/meetings last minute.