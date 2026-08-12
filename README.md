# JsAndroid · JS ↔ Android 交互实验室

面向 Hybrid / WebView 场景的**可运行示例集**：升级到 AndroidX + AGP 8.x，启动即加载本地 [`assets/demo.html`](app/src/main/assets/demo.html)，覆盖从「注入桥」到「系统能力」的经典交互。

> 适合：想搞清楚 H5 和原生到底怎么通信、各有什么坑的人。不是金融业务壳，是教学向演示。

## 60 秒上手

```bash
./gradlew assembleDebug
```

Android Studio 打开工程 → Run。应看到「JsAndroid 交互实验室」，而不是远程网页。

顶部按钮：

| 按钮 | 作用 |
|------|------|
| Native→JS | Native 主动 `evaluateJavascript` 推送 JSON |
| 重置 | 清空状态与页面日志 |
| 旧H5壳 | 打开遗留容器（旁路） |

Chrome 远程调试：电脑打开 `chrome://inspect` → 选择本机 WebView。

## 经典场景覆盖清单

| # | 场景 | 怎么触发 | 机制 |
|---|------|----------|------|
| 1 | JS 调 Native（无返回） | Toast / 对话框 / 震动 | `@JavascriptInterface` |
| 2 | JS 调 Native（有返回） | 设备信息 / App 信息 / 网络 | 同步返回 JSON 字符串 |
| 3 | 传结构化参数 | `postJson(...)` | Native 解析 `JSONObject` |
| 4 | 异步回调 | `callNativeAsync` | Native → `evaluateJavascript(cb)` |
| 5 | Promise 封装 | `NativeCall().then()` | callbackName 映射 Promise |
| 6 | Native Confirm/Prompt | 桥方法 + 回调 | 比网页 confirm 更可控 |
| 7 | Native → JS 主动推送 | 顶部「Native→JS」 | `evaluateJavascript` |
| 8 | `alert/confirm/prompt` | 网页原生 API | `WebChromeClient` 拦截 |
| 9 | `console.log` | 控制台输出 | `onConsoleMessage` 回传页面 |
| 10 | 加载进度 | 自动 | `onProgressChanged` |
| 11 | URL Scheme | `jsbridge://…` / `tel:` / `mailto:` | `shouldOverrideUrlLoading` |
| 12 | 外开系统浏览器 | Scheme 或 `openExternal` | `ACTION_VIEW` |
| 13 | 剪贴板 / 分享 | 系统能力区 | Clipboard / `ACTION_SEND` |
| 14 | 拨号 / 短信 / 邮件 | 系统能力区 | `ACTION_DIAL` 等 |
| 15 | 选文件（桥） | `pickFile()` | Intent + 回调 |
| 16 | 选文件（input） | `<input type=file>` | `onShowFileChooser` |
| 17 | 下载监听 | 点击下载示例 | `DownloadListener` |
| 18 | 运行时权限 + 扫码 | 扫码区 | CAMERA + ZXing |
| 19 | 页面控制 | goBack / reload / close | WebView / Activity |
| 20 | localStorage | 存储区 | `domStorageEnabled` |

桥对象：`window.NativeBridge`（实现见 [`NativeBridge.java`](app/src/main/java/com/example/cheng/js/NativeBridge.java)）

## API 速查

### 同步 / UI

```js
NativeBridge.showToast('hi')
NativeBridge.showDialog('标题', '内容')
NativeBridge.setNativeStatus('更新顶部 TextView')
NativeBridge.setTitle('来自 H5 的标题')
NativeBridge.vibrate(50)
```

### 有返回值

```js
JSON.parse(NativeBridge.getDeviceInfo())
JSON.parse(NativeBridge.getAppInfo())
JSON.parse(NativeBridge.getNetworkInfo())
```

### JSON / 异步

```js
NativeBridge.postJson(JSON.stringify({ name: 'JsAndroid', count: 1 }))

// 回调名方式
window.cb = (result) => console.log(result)
NativeBridge.callNativeAsync('ping', 'payload', 'cb')

// Promise 包装（demo.html 内置）
NativeCall('action', 'payload').then(console.log)

NativeBridge.showConfirm('确认', '继续？', 'cb')   // cb(true/false)
NativeBridge.showPrompt('昵称', 'Guest', 'cb')    // cb(text|null)
```

### 系统 Intent

```js
NativeBridge.copyToClipboard('text')
NativeBridge.readClipboard()
NativeBridge.shareText('分享内容')
NativeBridge.openExternal('https://example.com')
NativeBridge.dial('10086')
NativeBridge.sendSms('10086', '正文')
NativeBridge.sendEmail('a@b.com', '主题', '正文')
NativeBridge.startScan()
NativeBridge.pickFile()
```

### 页面控制

```js
NativeBridge.canGoBack()
NativeBridge.goBack()
NativeBridge.reload()
NativeBridge.closePage()
```

### URL Scheme（无需桥对象）

```
jsbridge://scan
jsbridge://route?lng=116.40&lat=39.91
jsbridge://toast?msg=Hello
jsbridge://browser?url=https%3A%2F%2Fdeveloper.android.com
tel:10086
mailto:demo@example.com
```

### Native 注入到页面的回调

| 函数 | 来源 |
|------|------|
| `onNativeMessage(obj)` | 顶部按钮推送 |
| `setScanResult(text)` | 扫码成功 |
| `onRouteResult(lng, lat)` | Scheme 坐标 |
| `onFilePicked(uri)` | 桥选文件 |
| `onConsoleFromNative(line)` | console 拦截回传 |
| `onLoadProgress(p)` | 0–100 |
| `onDownloadRequested(url, mime)` | 下载监听 |

## Native 推荐写法

```java
WebSettings s = webView.getSettings();
s.setJavaScriptEnabled(true);
s.setDomStorageEnabled(true);
WebView.setWebContentsDebuggingEnabled(true); // 仅 debug

webView.addJavascriptInterface(new NativeBridge(host), "NativeBridge");

// Android → JS：优先 evaluateJavascript，避免 loadUrl("javascript:...")
webView.evaluateJavascript("onNativeMessage({ok:true})", null);
```

注意：

1. `@JavascriptInterface` 注解不能少（API 17+）。
2. 回调里碰 UI 必须切主线程。
3. 向 JS 传字符串务必转义（本项目用 `NativeBridge.jsonStringLiteral`）。
4. 自定义 Scheme 大小写要统一；本项目统一小写 `jsbridge://`。
5. 文件选择要完整实现 `onShowFileChooser` + `onActivityResult` 回传，否则 `<input type=file>` 会卡住。
6. 扫码等能力需要 Manifest 权限 + 运行时申请。

## 工程结构

```
app/src/main/
├── assets/demo.html          # 可点击实验室（主演示）
├── java/com/example/cheng/js/
│   ├── MainActivity.java     # WebView 容器 + Chrome/Scheme/下载
│   ├── NativeBridge.java     # 注入桥 API
│   ├── TouguShowH5Activity   # 旧壳（保留）
│   ├── BaseInterface.java    # 业务 stub（文档向，未实现）
│   └── TouguInterface.java
└── AndroidManifest.xml
```

## 环境

- JDK 17+（Android Studio 自带 JBR 即可）
- Android SDK 34
- `local.properties` 配置本机 `sdk.dir`（勿提交）

## 刻意没做的（避免跑偏）

- 未实现完整金融 `BaseInterface` / `TouguInterface` 业务
- 未引入 JsBridge 第三方库（便于看清原生机制）
- 未上 Kotlin / Compose（保持示例直白）

若要生产级方案，可在理解本示例后换成官方推荐封装或成熟 JsBridge，并补鉴权、域名白名单、Bridge 方法鉴权。
