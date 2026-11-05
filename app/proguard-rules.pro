# Add project specific ProGuard rules here.
# Minification is off by default (see app/build.gradle.kts) so these rules
# are inactive until isMinifyEnabled is set to true — kept here so they're
# ready if you turn it on for a release build.

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keepattributes Signature
-keepattributes Exceptions

# Gson — keep the DTOs it reflects over
-keep class com.nourishlink.app.data.remote.dto.** { *; }
-keepattributes *Annotation*

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Firebase Messaging
-keep class com.google.firebase.messaging.** { *; }
