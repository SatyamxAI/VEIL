# VEIL ProGuard and R8 Rules

# Preserve SQLCipher JNI and SQLite bindings
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-keepclassmembers class * extends net.sqlcipher.database.SQLiteOpenHelper {
    public <init>(...);
}

# Preserve Room generated DAOs and entities
-keep class androidx.room.** { *; }
-dontwarn androidx.room.paging.**

# Gson reflection preservation for metadata envelope models
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.veil.core.model.** { *; }

# WorkManager Worker classes
-keep class * extends androidx.work.Worker {
    public <init>(...);
}
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(...);
}

# Android Keystore and Cryptographic providers
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }
