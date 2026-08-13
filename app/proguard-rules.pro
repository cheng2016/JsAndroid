# JsAndroid ProGuard / R8 rules for WebView JS bridge

-keepattributes JavascriptInterface
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Keep all @JavascriptInterface methods on bridge classes
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.example.cheng.js.NativeBridge { *; }
-keep class com.example.cheng.js.NativeBridge$* { *; }

# Optional: keep stub interface method names if ever registered
-keep interface com.example.cheng.js.BaseInterface { *; }
-keep interface com.example.cheng.js.TouguInterface { *; }
