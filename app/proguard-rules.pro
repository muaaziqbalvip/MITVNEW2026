# MITV ProGuard rules
-keep class com.mitv.master.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn com.google.firebase.**
-keep class androidx.media3.** { *; }

# Official NewPipeExtractor ProGuard rules (required — Rhino JS engine
# is used internally for YouTube signature/throttling deobfuscation).
-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.classfile.ClassFileWriter
-dontwarn org.mozilla.javascript.tools.**

# Rhino optionally references java.beans.* (java.desktop module) which
# doesn't exist on Android. That code path is never exercised on-device,
# so it's safe to silence rather than fail the build.
-dontwarn java.beans.**

