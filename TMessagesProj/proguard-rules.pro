
-keep class org.cafemember.SQLite.SQLitePreparedStatement
-keep class org.cafemember.SQLite.SQLiteNoRowException
-keep class org.cafemember.SQLite.SQLiteException
-keep class org.cafemember.SQLite.SQLiteDatabase
-keep class org.cafemember.SQLite.SQLiteCursor



-keep public class com.google.android.gms.* { public *; }
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}
-dontwarn com.google.android.gms.**
-dontwarn com.google.common.cache.**
-dontwarn com.google.common.primitives.**





-keepattributes Signature
-keepattributes Annotation
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.json.**

-keep class io.nivad.** { *; }
-keep class com.auth0.jwt.** { *; }
-dontwarn org.**
-dontwarn javax.**
-dontwarn com.auth0.jwt.**
-dontwarn com.squareup.okhttp.**

-keep class org.cafemember.messenger.mytg.ui.** { *; }
-keep class org.cafemember.ui.** { *; }











-dontwarn com.googlecode.mp4parser.**