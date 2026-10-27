# kotlinx.serialization keeps generated serializers and companions.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.aeeL.theokntl.**$$serializer { *; }
-keepclassmembers class com.aeeL.theokntl.** {
    *** Companion;
}
-keepclasseswithmembers class com.aeeL.theokntl.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp / Okio
-dontwarn okhttp3.**
-dontwarn okio.**

# Hilt and Dagger ship their own consumer rules.
