package com.example.cheng.js;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.webkit.JavascriptInterface;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * JS 可调用桥：注册为 window.NativeBridge
 * <p>
 * 覆盖经典场景：同步调用、返回值、JSON、异步回调、系统能力、页面控制等。
 */
public class NativeBridge {

    public interface Host {
        Activity getActivity();

        void updateStatus(String text);

        void evaluateJavascript(String script);

        void startQrScan();

        void openFilePicker();

        void reloadWebView();

        void goBackWebView();

        boolean canGoBackWebView();

        void closePage();

        void openExternalUrl(String url);

        void setActivityTitle(String title);

        /** Whether current page is allowed to call sensitive APIs */
        boolean isTrustedPage();

        void requestLocation(String callbackName);

        void pickImageAsBase64(String callbackName);

        String getCookie(String url);

        void setCookie(String url, String cookie);
    }

    private final Host host;

    public NativeBridge(Host host) {
        this.host = host;
    }

    // ---------- UI / Feedback ----------

    @JavascriptInterface
    public void showToast(final String message) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(host.getActivity(), message == null ? "" : message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public void showDialog(final String title, final String message) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(host.getActivity())
                        .setTitle(title == null ? "提示" : title)
                        .setMessage(message == null ? "" : message)
                        .setPositiveButton("确定", null)
                        .show();
            }
        });
    }

    /**
     * Native confirm：结果通过 callbackName(true/false) 回传
     */
    @JavascriptInterface
    public void showConfirm(final String title, final String message, final String callbackName) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(host.getActivity())
                        .setTitle(title == null ? "确认" : title)
                        .setMessage(message == null ? "" : message)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                invokeBoolCallback(callbackName, true);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                invokeBoolCallback(callbackName, false);
                            }
                        })
                        .setCancelable(true)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                invokeBoolCallback(callbackName, false);
                            }
                        })
                        .show();
            }
        });
    }

    /**
     * Native prompt：结果通过 callbackName(text|null) 回传
     */
    @JavascriptInterface
    public void showPrompt(final String title, final String defaultText, final String callbackName) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                final EditText input = new EditText(host.getActivity());
                input.setText(defaultText == null ? "" : defaultText);
                new AlertDialog.Builder(host.getActivity())
                        .setTitle(title == null ? "输入" : title)
                        .setView(input)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                invokeStringCallback(callbackName, input.getText().toString());
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                invokeStringCallback(callbackName, null);
                            }
                        })
                        .show();
            }
        });
    }

    @JavascriptInterface
    public void setNativeStatus(final String text) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                host.updateStatus(text);
            }
        });
    }

    @JavascriptInterface
    public void setTitle(final String title) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                host.setActivityTitle(title);
            }
        });
    }

    @JavascriptInterface
    public void vibrate(final int milliseconds) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                int ms = milliseconds <= 0 ? 40 : Math.min(milliseconds, 2000);
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        VibratorManager vm = (VibratorManager) host.getActivity()
                                .getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                        if (vm != null) {
                            vm.getDefaultVibrator().vibrate(
                                    VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
                        }
                    } else {
                        Vibrator v = (Vibrator) host.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        if (v != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                v.vibrate(ms);
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }

    // ---------- Device / App info ----------

    @JavascriptInterface
    public String getDeviceInfo() {
        JSONObject json = new JSONObject();
        try {
            json.put("manufacturer", Build.MANUFACTURER);
            json.put("model", Build.MODEL);
            json.put("sdkInt", Build.VERSION.SDK_INT);
            json.put("release", Build.VERSION.RELEASE);
            json.put("brand", Build.BRAND);
            json.put("device", Build.DEVICE);
        } catch (JSONException ignored) {
        }
        return json.toString();
    }

    @JavascriptInterface
    public String getAppInfo() {
        JSONObject json = new JSONObject();
        Activity act = host.getActivity();
        try {
            PackageManager pm = act.getPackageManager();
            PackageInfo info = pm.getPackageInfo(act.getPackageName(), 0);
            json.put("packageName", act.getPackageName());
            json.put("versionName", info.versionName);
            json.put("versionCode", Build.VERSION.SDK_INT >= 28
                    ? info.getLongVersionCode()
                    : info.versionCode);
        } catch (Exception e) {
            try {
                json.put("error", e.getMessage());
            } catch (JSONException ignored) {
            }
        }
        return json.toString();
    }

    @JavascriptInterface
    public String getNetworkInfo() {
        JSONObject json = new JSONObject();
        try {
            ConnectivityManager cm = (ConnectivityManager) host.getActivity()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            String type = "none";
            boolean connected = false;
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    android.net.Network network = cm.getActiveNetwork();
                    NetworkCapabilities caps = network != null ? cm.getNetworkCapabilities(network) : null;
                    if (caps != null) {
                        connected = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            type = "wifi";
                        } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            type = "cellular";
                        } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                            type = "ethernet";
                        } else {
                            type = "other";
                        }
                    }
                } else {
                    NetworkInfo ni = cm.getActiveNetworkInfo();
                    if (ni != null && ni.isConnected()) {
                        connected = true;
                        type = ni.getTypeName() == null ? "other" : ni.getTypeName().toLowerCase();
                    }
                }
            }
            json.put("connected", connected);
            json.put("type", type);
        } catch (JSONException ignored) {
        }
        return json.toString();
    }

    // ---------- Data ----------

    @JavascriptInterface
    public void postJson(final String json) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject obj = new JSONObject(json == null ? "{}" : json);
                    String name = obj.optString("name", "(unknown)");
                    int count = obj.optInt("count", 0);
                    host.updateStatus("JSON: name=" + name + ", count=" + count);
                    Toast.makeText(host.getActivity(), "已解析 JSON", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    host.updateStatus("JSON 解析失败: " + e.getMessage());
                }
            }
        });
    }

    /**
     * 异步：Native 处理后调用 window[callbackName](resultObject)
     */
    @JavascriptInterface
    public void callNativeAsync(final String action, final String payload, final String callbackName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(600);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                final String result;
                try {
                    JSONObject out = new JSONObject();
                    out.put("ok", true);
                    out.put("action", action == null ? "" : action);
                    out.put("echo", payload == null ? "" : payload);
                    out.put("from", "NativeBridge");
                    out.put("ts", System.currentTimeMillis());
                    result = out.toString();
                } catch (JSONException e) {
                    return;
                }
                runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        String safeName = sanitizeCallback(callbackName);
                        if (safeName == null) {
                            return;
                        }
                        host.evaluateJavascript("typeof window['" + safeName + "']==='function'&&window['"
                                + safeName + "'](" + result + ")");
                    }
                });
            }
        }).start();
    }

    // ---------- System intents ----------

    @JavascriptInterface
    public void copyToClipboard(final String text) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                ClipboardManager cm = (ClipboardManager) host.getActivity()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    cm.setPrimaryClip(ClipData.newPlainText("jsbridge", text == null ? "" : text));
                    Toast.makeText(host.getActivity(), "已复制到剪贴板", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @JavascriptInterface
    public String readClipboard() {
        ClipboardManager cm = (ClipboardManager) host.getActivity()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null || cm.getPrimaryClip() == null || cm.getPrimaryClip().getItemCount() == 0) {
            return "";
        }
        CharSequence text = cm.getPrimaryClip().getItemAt(0).coerceToText(host.getActivity());
        return text == null ? "" : text.toString();
    }

    @JavascriptInterface
    public void shareText(final String text) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, text == null ? "" : text);
                host.getActivity().startActivity(Intent.createChooser(intent, "分享到"));
            }
        });
    }

    @JavascriptInterface
    public void openExternal(final String url) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                host.openExternalUrl(url);
            }
        });
    }

    @JavascriptInterface
    public void dial(final String phone) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                if (phone == null || phone.length() == 0) {
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                try {
                    host.getActivity().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(host.getActivity(), "无法打开拨号盘", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @JavascriptInterface
    public void sendSms(final String phone, final String body) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                Uri uri = Uri.parse("smsto:" + (phone == null ? "" : phone));
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                intent.putExtra("sms_body", body == null ? "" : body);
                try {
                    host.getActivity().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(host.getActivity(), "无法打开短信", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @JavascriptInterface
    public void sendEmail(final String to, final String subject, final String body) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + (to == null ? "" : to)));
                intent.putExtra(Intent.EXTRA_SUBJECT, subject == null ? "" : subject);
                intent.putExtra(Intent.EXTRA_TEXT, body == null ? "" : body);
                try {
                    host.getActivity().startActivity(Intent.createChooser(intent, "发送邮件"));
                } catch (Exception e) {
                    Toast.makeText(host.getActivity(), "无法打开邮件应用", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ---------- Media / scan / file ----------

    @JavascriptInterface
    public void startScan() {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                host.startQrScan();
            }
        });
    }

    @JavascriptInterface
    public void pickFile() {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                host.openFilePicker();
            }
        });
    }

    // ---------- Page control ----------

    @JavascriptInterface
    public void reload() {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                host.reloadWebView();
            }
        });
    }

    @JavascriptInterface
    public void goBack() {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                host.goBackWebView();
            }
        });
    }

    @JavascriptInterface
    public boolean canGoBack() {
        return host.canGoBackWebView();
    }

    @JavascriptInterface
    public void closePage() {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                host.closePage();
            }
        });
    }

    // ---------- Cookie / Location / Image / Auth ----------

    @JavascriptInterface
    public String getCookie(String url) {
        String value = host.getCookie(url);
        return value == null ? "" : value;
    }

    @JavascriptInterface
    public void setCookie(final String url, final String cookie) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                host.setCookie(url, cookie);
                Toast.makeText(host.getActivity(), "Cookie 已写入", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public void getLocation(final String callbackName) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                host.requestLocation(callbackName);
            }
        });
    }

    @JavascriptInterface
    public void pickImageBase64(final String callbackName) {
        runOnUi(new Runnable() {
            @Override
            public void run() {
                host.pickImageAsBase64(callbackName);
            }
        });
    }

    /**
     * Production-style gate: requires demo token + trusted page origin.
     * Demo token: demo-token-jsandroid
     */
    @JavascriptInterface
    public String secureCall(String token, String action, String payload) {
        JSONObject out = new JSONObject();
        try {
            if (!host.isTrustedPage()) {
                out.put("ok", false);
                out.put("error", "untrusted_origin");
                return out.toString();
            }
            if (!"demo-token-jsandroid".equals(token)) {
                out.put("ok", false);
                out.put("error", "unauthorized");
                return out.toString();
            }
            out.put("ok", true);
            out.put("action", action == null ? "" : action);
            out.put("echo", payload == null ? "" : payload);
            out.put("from", "secureCall");
            out.put("ts", System.currentTimeMillis());
        } catch (JSONException ignored) {
        }
        return out.toString();
    }

    @JavascriptInterface
    public boolean isTrustedPage() {
        return host.isTrustedPage();
    }

    // ---------- helpers ----------

    private void invokeBoolCallback(String callbackName, boolean value) {
        String safe = sanitizeCallback(callbackName);
        if (safe == null) {
            return;
        }
        host.evaluateJavascript("typeof window['" + safe + "']==='function'&&window['"
                + safe + "'](" + value + ")");
    }

    private void invokeStringCallback(String callbackName, String value) {
        String safe = sanitizeCallback(callbackName);
        if (safe == null) {
            return;
        }
        host.evaluateJavascript("typeof window['" + safe + "']==='function'&&window['"
                + safe + "'](" + jsonStringLiteral(value) + ")");
    }

    private static String sanitizeCallback(String callbackName) {
        if (callbackName == null || callbackName.length() == 0) {
            return null;
        }
        String safe = callbackName.replaceAll("[^A-Za-z0-9_$]", "");
        return safe.length() == 0 ? null : safe;
    }

    private void runOnUi(Runnable r) {
        host.getActivity().runOnUiThread(r);
    }

    static String jsonStringLiteral(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\').append(c);
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
