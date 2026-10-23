# Add project specific ProGuard rules here.

# Keep Kotlinx Serialization classes
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Firebase Remote Config models
-keepclassmembers class * {
    @com.google.firebase.remoteconfig.* <fields>;
}

# Keep Domain Models & Entities
-keep class com.enterprise.expensetracker.core.model.** { *; }

# Keep Dagger / Hilt generated classes
-keep class * extends dagger.internal.Binding { *; }
-keep class * extends dagger.internal.Factory { *; }
-keep class * extends dagger.internal.MembersInjector { *; }
