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

### Contributing
You can contribute anything via pull-requests. Currently, this shows how Courier can work together with Google Calendar.
You can improve on this solution, or add any other cross-app scripts.

### License
MIT License

Copyright (c) 2022 Henk Grent

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
