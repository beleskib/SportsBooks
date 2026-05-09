# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.example.sportsbook.**$$serializer { *; }
-keepclassmembers class com.example.sportsbook.** { *** Companion; }
-keepclasseswithmembers class com.example.sportsbook.** { kotlinx.serialization.KSerializer serializer(...); }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Stripe
-keep class com.stripe.** { *; }
-dontwarn com.stripe.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Keep data/model classes
-keep class com.example.sportsbook.data.remote.dto.** { *; }
-keep class com.example.sportsbook.domain.model.** { *; }
-keep class com.example.sportsbook.domain.enums.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Compose Navigation serializable routes
-keep class com.example.sportsbook.ui.navigation.Route$* { *; }
