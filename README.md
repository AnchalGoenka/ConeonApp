**📦 QuickDrop – Real-Time Delivery Tracker**

A Kotlin-based Android app that enables real-time delivery tracking using live location updates, foreground services, Socket.IO, and Google Maps.



🚀 **Features**

🔐 User Authentication With Firebase

📦 Mock Order Placement (you can integrate this)

🗺️ Live Location Tracking (ForegroundService + FusedLocationProvider)

📡 Socket.IO Integration for broadcasting delivery partner's location

📍 Google Maps Display with toggle tracking support

🔕 Push Notification for background tracking updates

🔁 Foreground Notification with PendingIntent support

🔒 Permission Handling at runtime



**⚙️ Setup Instructions**

Replace https://your-socket-server.com in LocationForegroundService.kt with your own Socket.IO endpoint.

Add Google Maps API key in AndroidManifest.xml.

Enable Maps SDK for Android and Directions API in your Google Cloud project.

Install the app on a physical device or an emulator with Google Play Services.
