# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/wanglei/Library/Android/sdk/tools/proguard/proguard-android.txt
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

-keep class com.xmtj.bpgsdkdemo.downloader.glide.OkHttpGlideModule
#BPG 混淆控制
-dontwarn com.xmtj.bpgdecoder.**
-keep public class com.xmtj.bpgdecoder.**{*;}
