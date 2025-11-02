# Street Light Monitor - Android App
A real-time monitoring application for street light status detection with cloud analytics integration.
Features
✨ Real-Time Monitoring

Live street light status (OFF/ON/FLICKER)
Color-coded indicators for quick identification
Auto-refresh every 30 seconds

📊 Data Analytics

Historical data visualization with interactive charts
Light level and standard deviation trends
View 24-hour, 7-day, or 30-day data

🔔 Push Notifications

Instant alerts on status changes
Critical fault notifications (FLICKER detected)
Customizable notification settings

🎨 Modern UI Design

Soft color palette with Material Design
Clean, intuitive interface
Smooth animations and transitions

![WhatsApp Image 2025-11-02 at 14 36 33](https://github.com/user-attachments/assets/55f840d8-4caa-4d9b-a4e0-79ecc4f0a1c3)
![WhatsApp Image 2025-11-02 at 14 36 32](https://github.com/user-attachments/assets/5fd7e57a-5b0f-4c96-8d80-be9eb56b77d6)


### Tech Stack

Language: Kotlin
Architecture: MVVM with Repository pattern
Networking: Retrofit + OkHttp
UI: Material Design Components
Charts: MPAndroidChart
Async: Kotlin Coroutines + LiveData
Backend: ThingSpeak REST API

```
[Live Status Screen]    [Analytics Screen]
     Green: ON         Historical Charts
     Red: OFF          Light Levels
     Yellow: FLICKER   Trend Analysis
```
### Installation

Clone the repository

`bashgit clone https://github.com/yourusername/street-light-monitor.git`

Open project in Android Studio
Update ThingSpeak credentials in SensorRepository.kt:

`kotlinprivate val channelId = "YOUR_CHANNEL_ID"`
`private val readApiKey = "YOUR_READ_API_KEY" // if private`

Build and run on device (Android 7.0+)

### Configuration
ThingSpeak Setup:

Channel ID: 3089109 (update with your channel)
```
Field 1: Light Level (raw sensor value)
Field 2: Standard Deviation (stability metric)
Field 3: Status Code (0=OFF, 1=ON, 2=FLICKER)
```

### API Endpoints:
```
GET https://api.thingspeak.com/channels/{channelId}/feeds.json?results=20
GET https://api.thingspeak.com/channels/{channelId}/feeds/last.json
```

### Permissions Required
```
xml<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Key Components
MainActivity.kt

Navigation between fragments
Wi-Fi status monitoring
Bottom navigation bar

FirstFragment.kt (Analytics)

Historical data visualization
MPAndroidChart integration
Data refresh logic

SecondFragment.kt (Live Status)

Real-time status display
Push notification triggers
Status change animations

SensorRepository.kt

ThingSpeak API calls
Data parsing and caching
Error handling

NotificationManager.kt

Status change detection
Push notification creation
Alert prioritization

### Usage

Launch App → Connects to ThingSpeak automatically
View Live Status → Tap "Live Status" to see current condition
Check Analytics → Tap "Analytics" for historical charts
Get Alerts → Notifications sent on status changes

### Build Information

Min SDK: 24 (Android 7.0)
Target SDK: 34 (Android 14)
Kotlin Version: 1.9.0
Gradle: 8.2.0

Dependencies
```kt
kotlin// Networking
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

// Charts
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'

// Material Design
implementation 'com.google.android.material:material:1.10.0'
```

### Troubleshooting
Issue: No data showing

Solution: Check ThingSpeak channel ID and API key
Verify ESP32 is uploading data

Issue: Notifications not working

Solution: Grant notification permission in Android settings
Check Android version (13+ requires runtime permission)

Issue: Chart not loading

Solution: Ensure internet connection
Check if ThingSpeak has data (min 2 data points needed)

