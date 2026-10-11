-injars      build/classes
-outjars     build/preverified

-libraryjars lib/cldcapi11.jar
-libraryjars lib/midpapi20.jar
-libraryjars lib/microemu-nokiaui.jar
-libraryjars lib/microemu-jsr-75.jar
-libraryjars lib/kxml2-2.3.0.jar
-libraryjars lib/jzlib-1.1.3.jar

-dontoptimize
-dontshrink
-dontobfuscate

-keep public class * extends javax.microedition.midlet.MIDlet {
    public <init>();
}
-keep class com.ereader.** { *; }
