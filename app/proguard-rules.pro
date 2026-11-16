# Cyber Tarot / WEED GURU ProGuard & R8 Keep Rules (H5)

# Keep Room entities & DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# Keep Moshi / Retrofit JSON-RPC DTOs & Kotlin metadata
-keep class kotlin.Metadata { *; }
-keepclassmembers class com.example.data.JsonRpcRequest { *; }
-keepclassmembers class com.example.data.JsonRpcResponse { *; }
-keepclassmembers class com.example.data.JsonRpcError { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Retrofit interface methods
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Strip verbose debug logs in minified builds
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# Keep Firebase Firestore / Auth models & SDK reflection
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keepclassmembers class com.example.data.* { *; }

# Keep Jetpack Credential Manager & Google Identity
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**

# Keep Kotlin Coroutines debug/service loader
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

