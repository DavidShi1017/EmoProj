# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\apps\develop\android-sdk\sdk/tools/proguard/proguard-android.txt
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

# 第三方库
-dontwarn android.support.**
-dontwarn com.alibaba.fastjson.**
-dontwarn com.squareup.picasso.**
# -dontwarn com.tencent.**

-keep class com.tencent.** { *; }
-keep class com.sina.** { *; }
# 以下包不进行过滤 友盟
-keep class com.umeng.** { *; }

# 假定代码无效，达到删除日志代码的效果
-assumenosideeffects class cn.ablecloud.emo.utils.LogUtils {
	public static void d(...);
}

-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}