# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve Retrofit/Gson DTO fields and annotations under R8 shrinking
-keepattributes Signature, RuntimeVisibleAnnotations, AnnotationDefault
-keep class io.github.amirisback.androidapp.data.remote.dto.** { *; }
