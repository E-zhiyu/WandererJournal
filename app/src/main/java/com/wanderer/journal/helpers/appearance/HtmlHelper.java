package com.wanderer.journal.helpers.appearance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wanderer.journal.auxiliary.enums.LogTags;

import java.util.List;

public class HtmlHelper {
    private AndroidBridge bridge;

    public interface ImageGenerateListener {
        /**
         * 开始生成图片回调
         */
        void onLoadingStart();

        /**
         * DOM 正在注入回调
         *
         * @param current 注入完成的分段数量
         * @param total   总共需要注入的分段数量
         */
        void onLoading(int current, int total);

        /**
         * DOM 完全加载完毕回调
         *
         * @param bridge JS 桥梁
         * @param total  总共的段落数
         */
        void onDomFinished(AndroidBridge bridge, int total);

        /**
         * 图片生成错误回调
         *
         * @param message 错误信息
         */
        void onError(String message);
    }

    /**
     * 生成图片
     *
     * @param jsonData 分好段的 JSON 字符串列表
     * @param context  上下文
     * @param listener 图片生成状态监听器
     */
    @SuppressLint("SetJavaScriptEnabled")
    public void generateImage(List<String> jsonData, Context context, ImageGenerateListener listener) {
        if (listener == null) throw new IllegalArgumentException("监听器不能为空");

        //判空
        if (jsonData.isEmpty()) {
            listener.onError("JSON数据为空");
            return;
        } else {
            listener.onLoadingStart();
        }

        WebView.enableSlowWholeDocumentDraw();

        //在内存中动态创建 WebView
        WebView backgroundWebView = new WebView(context);

        //配置 WebView 属性
        backgroundWebView.getSettings().setJavaScriptEnabled(true);
        backgroundWebView.getSettings().setUseWideViewPort(true);
        backgroundWebView.getSettings().setLoadWithOverviewMode(true);
        backgroundWebView.getSettings().setSupportZoom(false);      //禁用缩放
        backgroundWebView.getSettings().setAllowFileAccess(true);   //允许加载本地文件
        backgroundWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        backgroundWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        //动态获取当前手机屏幕的真实物理像素宽度 (px)
        int screenWidthPx = ViewEdgeHelper.getScreenWidth(context);

        //用动态算出的屏幕宽度去强行测绘 WebView
        backgroundWebView.measure(
                View.MeasureSpec.makeMeasureSpec(screenWidthPx, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        backgroundWebView.layout(0, 0, screenWidthPx, 20);

        //注入 JS 桥梁
        bridge = new AndroidBridge(backgroundWebView, jsonData, listener);
        backgroundWebView.addJavascriptInterface(
                bridge,
                "AndroidShareBridge"
        );

        //监听加载完成
        backgroundWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (bridge != null) {
                    bridge.processNextPart();
                }
            }
        });

        //打印 WebView 的日志
        backgroundWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(LogTags.SHARE_WEB_VIEW.n(), consoleMessage.message() + " -- Line "
                        + consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
                return true;
            }
        });

        // 5. 开始离线加载本地 HTML 模板
        backgroundWebView.loadUrl("file:///android_asset/share_template.html");
    }

    /**
     * JS 交互桥梁
     */
    public static class AndroidBridge {
        private WebView backgroundWebView;
        private final List<String> jsonData;
        private final ImageGenerateListener listener;
        private int currentPart;    //当前所处的分段的下标

        /**
         * WebView 的 JS 代码桥梁
         *
         * @param backgroundWebView WebView实例
         * @param listener          图片加载状态监听器
         */
        public AndroidBridge(WebView backgroundWebView, List<String> jsonData, ImageGenerateListener listener) {
            this.backgroundWebView = backgroundWebView;
            this.jsonData = jsonData;
            this.listener = listener;
        }

        public WebView getBackgroundWebView() {
            return backgroundWebView;
        }

        /**
         * 注入下一个分段的 DOM 数据
         */
        public void processNextPart() {
            //判空
            if (backgroundWebView == null) return;

            // 使用 Base64 编码解决换行和引号问题
            String base64Data = Base64.encodeToString(jsonData.get(currentPart).getBytes(), Base64.NO_WRAP);
            String javascript = "javascript:initDataFromBase64('" + base64Data + "')";
            backgroundWebView.evaluateJavascript(javascript, null);

            currentPart++;
        }

        /**
         * WebView 渲染完毕回调（在WebView的 JS 代码中调用）
         */
        @JavascriptInterface
        public void onRenderFinished() {
            if (backgroundWebView == null) return;

            //判断所有数据是否全部注入完毕
            if (currentPart >= jsonData.size()) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onDomFinished(this, jsonData.size()));
            } else {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (listener != null) {
                        listener.onLoading(currentPart, jsonData.size());
                    }
                });
                new Handler(Looper.getMainLooper()).postDelayed(
                        this::processNextPart,
                        50 //延迟再注入，间隔期间用于更新 Android 端的UI
                );
            }
        }

        /**
         * 销毁 WebView
         */
        public void destroyWebView() {
            if (backgroundWebView != null) {
                backgroundWebView.loadUrl("about:blank");
                backgroundWebView.clearHistory();
                backgroundWebView.removeAllViews();
                backgroundWebView.destroy();
                backgroundWebView = null;
            }
        }
    }

    /**
     * 取消图片生成
     */
    public void cancelGenerateImage() {
        if (bridge != null) {
            bridge.destroyWebView();
            bridge = null;
        }
    }
}