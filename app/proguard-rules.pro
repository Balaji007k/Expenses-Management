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

# Keep MongoDB Driver & BSON
-keep class com.mongodb.** { *; }
-keep class org.bson.** { *; }
-dontwarn com.mongodb.**
-dontwarn org.bson.**

# Keep OkHttp & Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Keep App Entities & DAOs
-keep class com.enterprise.expensetracker.core.database.entity.** { *; }
-keep class com.enterprise.expensetracker.core.database.dao.** { *; }
-keep class com.enterprise.expensetracker.core.ai.** { *; }
