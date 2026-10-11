-injars      build/classes
-outjars     build/preverified

-libraryjars lib/cldcapi11.jar
-libraryjars lib/midpapi20.jar
-libraryjars lib/microemu-nokiaui.jar
-libraryjars lib/microemu-jsr-75.jar

-dontoptimize
-dontshrink
-dontobfuscate
-target 1.1
-microedition
-dontwarn com.jcraft.jzlib.**

-keep public class * extends javax.microedition.midlet.MIDlet {
    public <init>();
}
-keep class com.ereader.** { *; }
-keep class com.jcraft.jzlib.** { *; }
-keep class org.xmlpull.** { *; }
-keep class org.xmlpull.v1.** { *; }
-keep class org.kxml2.** { *; }
