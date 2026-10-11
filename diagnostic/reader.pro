-injars      ../build/reader-classes
-outjars     ../build/reader-preverified

-libraryjars ../lib/cldcapi11.jar
-libraryjars ../lib/midpapi20.jar
-libraryjars ../lib/microemu-nokiaui.jar
-libraryjars ../lib/microemu-jsr-75.jar

-dontoptimize
-dontshrink
-dontobfuscate
-target 1.1
-microedition

-keep public class * extends javax.microedition.midlet.MIDlet {
    public <init>();
}
-keep class com.ereader.** { *; }
