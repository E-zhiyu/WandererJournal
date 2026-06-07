package com.wanderer.journal.helpers.appearance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.helpers.file.MediaHelper;

import java.io.File;

public class HtmlHelper {
    public interface OnShareListener {
        /**
         * 开始生成图片回调
         */
        void onLoadingStart();

        /**
         * 图片生成完毕回调
         *
         * @param imageFile 生成的图片文件
         */
        void onShareReady(File imageFile);

        /**
         * 图片生成错误回调
         *
         * @param message 错误信息
         */
        void onError(String message);
    }

    /**
     * 外部调用的主入口
     */
    @SuppressLint("SetJavaScriptEnabled")
    public static void generateAndShare(String jsonData, Context context, OnShareListener listener) {
        if (listener != null) listener.onLoadingStart();

        WebView.enableSlowWholeDocumentDraw();

        //在内存中动态创建 WebView
        WebView backgroundWebView = new WebView(context);

        //配置 WebView 属性
        backgroundWebView.getSettings().setJavaScriptEnabled(true);
        backgroundWebView.getSettings().setUseWideViewPort(true);
        backgroundWebView.getSettings().setLoadWithOverviewMode(true);
        backgroundWebView.getSettings().setSupportZoom(false); // 禁用缩放

        //动态获取当前手机屏幕的真实物理像素宽度 (px)
        int screenWidthPx = ViewEdgeHelper.getScreenWidth(context);

        //用动态算出的屏幕宽度去强行测绘 WebView
        backgroundWebView.measure(
                View.MeasureSpec.makeMeasureSpec(screenWidthPx, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        backgroundWebView.layout(0, 0, screenWidthPx, 20);

        //注入 JS 桥梁
        backgroundWebView.addJavascriptInterface(
                new AndroidBridge(backgroundWebView, context, listener),
                "AndroidShareBridge"
        );

        //监听加载完成
        backgroundWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 使用 Base64 编码解决换行和引号问题
                String base64Data = Base64.encodeToString(jsonData.getBytes(), Base64.NO_WRAP);
                String javascript = "javascript:initDataFromBase64('" + base64Data + "')";
                backgroundWebView.evaluateJavascript(javascript, null);
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
    private static class AndroidBridge {
        private final WebView backgroundWebView;
        private final Context context;
        private final OnShareListener listener;

        /**
         * WebView 的 JS 代码桥梁
         *
         * @param backgroundWebView WebView实例
         * @param context           上下文
         * @param listener          图片加载状态监听器
         */
        public AndroidBridge(WebView backgroundWebView, Context context, OnShareListener listener) {
            this.backgroundWebView = backgroundWebView;
            this.context = context;
            this.listener = listener;
        }

        /**
         * WebView 渲染完毕回调（在WebView的 JS 代码中调用）
         */
        @JavascriptInterface
        public void onRenderFinished() {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (backgroundWebView == null) return;

                //网页里的 DOM 已经渲染完毕，开始后台截图
                float density = context.getResources().getDisplayMetrics().density;
                Log.d(LogTags.HTML_HELPER.n(), "屏幕密度：" + density);
                int contentHeight = backgroundWebView.getContentHeight();
                Bitmap bitmap = captureWebView(backgroundWebView, (int) (contentHeight * density));

                if (bitmap != null) {
                    //保存为文件并获取 Uri（复用之前写好的工具类方法）
                    File imageFile = MediaHelper.saveBitmapToFile(context, bitmap);
                    bitmap.recycle(); //及时释放内存

                    if (listener != null) {
                        listener.onShareReady(imageFile);
                    }
                } else {
                    if (listener != null) listener.onError("生成图片失败，内存不足");
                }

                //生成完图片后，彻底销毁内存中的 WebView 防止内存泄漏
                destroyWebView(backgroundWebView);
            });
        }

        /**
         * 后台测绘并将 WebView 转换为全量长图
         */
        @Nullable
        private Bitmap captureWebView(@NonNull WebView webView, int realHeight) {
            try {
                int screenWidthPx = ViewEdgeHelper.getScreenWidth(webView.getContext());
                webView.measure(screenWidthPx, realHeight);
                webView.layout(0, 0, screenWidthPx, realHeight);
                Log.d(LogTags.HTML_HELPER.n(), "宽度：" + screenWidthPx);
                Log.d(LogTags.HTML_HELPER.n(), "高度：" + realHeight);

                // 绘制到画布
                Bitmap bitmap = Bitmap.createBitmap(screenWidthPx, realHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                webView.scrollTo(0, 0); //滚动到顶部
                webView.draw(canvas);

                return bitmap;
            } catch (OutOfMemoryError e) {
                Log.e(LogTags.HTML_HELPER.n(), "内存不足，无法转换为图片");
                return null;
            }
        }

        /**
         * 销毁 WebView
         *
         * @param backgroundWebView 待销毁的 WebView
         */
        private void destroyWebView(WebView backgroundWebView) {
            if (backgroundWebView != null) {
                backgroundWebView.loadUrl("about:blank");
                backgroundWebView.clearHistory();
                backgroundWebView.removeAllViews();
                backgroundWebView.destroy();
            }
        }
    }
}