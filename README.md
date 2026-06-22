# Turf Widget

A home-screen widget that shows your [Turf](http://turfgame.com) stats
(points, points per hour, zones and ranking) and alerts you when you lose a
zone.

Original code by Drayon. For more info:
http://forum.turfa.se/viewtopic.php?f=55&t=117

## Project layout

This is a standard Android Gradle project:

```
settings.gradle          Gradle settings / module list
build.gradle             Top-level build (plugin versions)
app/                     The application module
  build.gradle           Module build config (SDK levels, dependencies)
  src/main/java          Sources (package com.turfgame.widget)
  src/main/res           Resources
  src/main/AndroidManifest.xml
```

### Source overview (`com.turfgame.widget`)

| Class | Responsibility |
| --- | --- |
| `TurfWidget` | App-widget provider; handles refresh / launch / alert actions |
| `UpdateWorker` | Runs a single refresh off the main thread (WorkManager) |
| `UpdateScheduler` / `UpdateAlarmReceiver` | Schedules and receives periodic refreshes |
| `net.TurfApi` / `net.TurfApiException` | Talks to the Turf HTTP API |
| `model.CharStats` | Immutable stats snapshot |
| `render.WidgetRenderer` / `render.CustomText` | Builds the widget `RemoteViews` |
| `Notifications` | Zone-loss notification (channel-aware) |
| `AlertState` | Persists zone-loss alert state across updates |
| `Settings` | Typed access to user preferences |
| `SettingsActivity` / `SettingsFragment` | Settings + widget configuration UI |

## Building

Open the project in a recent Android Studio (Giraffe+), or build from the
command line:

```
./gradlew assembleDebug
```

The Gradle wrapper properties are checked in; run `gradle wrapper` once (or let
Android Studio sync) to fetch the wrapper JAR if it is not present. Requires
JDK 17 and the Android SDK (compileSdk 34).
