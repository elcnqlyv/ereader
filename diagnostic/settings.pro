-injars      ../build/settings-classes
-outjars     ../build/settings-preverified

-libraryjars ../lib/cldcapi11.jar
-libraryjars ../lib/midpapi20.jar
-libraryjars ../lib/microemu-nokiaui.jar

-dontoptimize
-dontshrink
-dontobfuscate
-target 1.1
-microedition

-keep public class * extends javax.microedition.midlet.MIDlet {
    public <init>();
}
-keep class com.ereader.** { *; }
