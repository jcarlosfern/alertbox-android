-keepattributes Signature,*Annotation*
-keep class app.alertbox.io.core.model.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

