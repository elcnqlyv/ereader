-injars      build/classes
-outjars     build/preverified

-libraryjars lib/cldcapi11.jar
-libraryjars lib/midpapi20.jar
-libraryjars lib/microemu-nokiaui.jar

-microedition

-dontoptimize
-dontshrink
-dontobfuscate

-keep public class * extends javax.microedition.midlet.MIDlet {
    public <init>();
}
-keep class com.ereader.** { *; }
