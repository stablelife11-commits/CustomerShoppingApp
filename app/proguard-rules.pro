# Preserve line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*

# Keep all CustomerShoppingApp model DTOs and TypeTokens for Gson & Serializable
-keep class com.retail.customershoppingapp.model.** { *; }
-keepclassmembers class com.retail.customershoppingapp.model.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep classes & fields annotated with @SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit 2.x & OkHttp 4.x rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**