# SilentSync
SilentSync is an Android app that makes it easy to avoid being distracted by notifications and calls (except those in priority list) during important meetings by toggling DND (Do-Not-Disturb) mode based on calendar events and provided criteria. It works in the background even when the phone is in Doze mode.


## Tech Stack

**UI:** Jetpack Compose

**DI:** Koin

**Calendar:** Content Receiver, Android Content Provider

**Background Work:** Alarm Manager

**Database:** Room

**Project Structure:** Multi-Module

**Architecture Pattern:** MVI

## Features
- **Criteria options: Event titles, Attendee names**
- **Synchronization: Automatic DND toggle scheduling as soon as events matching criteria appear/change/get removed**
- **Ability to customize DND toggle for each individual event**
- **Overlap protection if one event ends at the same time as another one starts**

<img src = "https://github.com/user-attachments/assets/b3bb6796-13bf-467c-9f57-f3f874b07d17" height="900">

