# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/transcendent/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Use unique member names to make stack trace reading easier
-useuniqueclassmembernames

# retrofit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# otto
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

# retrolambda
-dontwarn java.lang.invoke.*

# mixpanel
-dontwarn com.mixpanel.**

# okio (from picasso?)
-dontwarn okio.**

# Joda Time 2.3 -- hopefully working for 2.9 too :)
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

# For RxJav:
-dontwarn org.mockito.**
-dontwarn org.junit.**
-dontwarn org.robolectric.*


##---------------Begin: proguard configuration for Gson  ----------
# Adapted from https://code.google.com/p/google-gson/source/browse/trunk/examples/android-proguard-example/proguard.cfg
#
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# We use Gson's @SerializedName annotation which won't work without this:
-keepattributes *Annotation*

# Gson specific classes
#-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }


# Classes that will be serialized/deserialized over Gson
# http://stackoverflow.com/a/7112371/56285
-keep class co.optonaut.optonaut.model.** { *; }

##---------------End: proguard configuration for Gson  ----------

# Remove logging calls
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# google data binding
-dontwarn android.databinding.**

##---------------Begin: proguard configuration for RxJava ----------
## source: https://github.com/artem-zinnatullin/RxJavaProGuardRules/blob/master/rxjava-proguard-rules/proguard-rules.txt
-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
##---------------End: proguard configuration for RxJava ----------

# Cardboard Sdk
-keep class com.google.vrtoolkit.cardboard.** { *; }

##------------------Begin: Butterknife------------
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}
##---------------------End: Butterknife -------------