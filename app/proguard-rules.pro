# Stylish Launcher ProGuard Rules

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class com.stylishlauncher.Hilt_* { *; }

# Keep Kotlin serialization
-keep class kotlinx.serialization.** { *; }
-keep class com.stylishlauncher.data.model.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
-keep class org.jetbrains.compose.** { *; }

# Keep Coil
-keep class coil.** { *; }
-keep class io.coil.** { *; }

# Keep Accompanist
-keep class com.google.accompanist.** { *; }

# Keep Material3
-keep class androidx.compose.material3.** { *; }

# Keep Hilt ViewModels
-keep class androidx.hilt.** { *; }

# Keep our classes
-keep class com.stylishlauncher.** { *; }

# Keep Parcelable/Serializable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep annotations
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}