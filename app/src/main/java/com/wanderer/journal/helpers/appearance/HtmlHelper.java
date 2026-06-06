package com.wanderer.journal.helpers.appearance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
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

public class HtmlHelper {
    public interface OnShareListener {
        /**
         * 开始生成图片回调
         */
        void onLoadingStart();

        /**
         * 图片生成完毕回调
         *
         * @param imageUri 生成的图片文件的 Uri
         */
        void onShareReady(Uri imageUri);

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

        // 1. 在内存中动态创建 WebView（不传入 Activity 的 layout）
        WebView backgroundWebView = new WebView(context);

        // 2. 配置 WebView 属性
        backgroundWebView.getSettings().setJavaScriptEnabled(true);
        backgroundWebView.getSettings().setUseWideViewPort(true);
        backgroundWebView.getSettings().setLoadWithOverviewMode(true);

        // 核心：强制给一个初始宽度（例如标准分享图宽度 1080 像素），以便让 HTML 内部有测量基准
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        backgroundWebView.measure(widthMeasureSpec, heightMeasureSpec);
        backgroundWebView.layout(0, 0, 1080, 0);

        // 3. 注入 JS 桥梁
        backgroundWebView.addJavascriptInterface(
                new AndroidBridge(backgroundWebView, context, listener),
                "AndroidShareBridge"
        );

        // 4. 监听加载完成
        backgroundWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // 【关键修复】在页面加载完成后，再次强制在主线程触发一次布局测量
                view.measure(
                        View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );
                view.layout(0, 0, 1080, view.getMeasuredHeight());

                // 网页模板加载完后，将 JSON 数据传过去
                // 方案 A：使用 Base64 编码（最稳妥，完美解决换行和引号问题）
                String base64Data = Base64.encodeToString(jsonData.getBytes(), Base64.NO_WRAP);
                String javascript = "javascript:initDataFromBase64('" + base64Data + "')";
                backgroundWebView.evaluateJavascript(javascript, null);
            }
        });

        //打印 WebView 的日志
        backgroundWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                // 这一步能让你在 Android Studio 的 Logcat 中直接看到网页内部的 console.log 和报错信息！
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

                // 网页里的 DOM 已经渲染完毕，开始后台截图
                Bitmap bitmap = captureWebView(backgroundWebView);

                if (bitmap != null) {
                    // 保存为文件并获取 Uri（复用之前写好的工具类方法）
                    Uri imageUri = ImageHelper.saveBitmapToFile(context, bitmap);
                    bitmap.recycle(); // 及时释放内存

                    if (listener != null) {
                        listener.onShareReady(imageUri);
                    }
                } else {
                    if (listener != null) listener.onError("生成图片失败，内存不足");
                }

                // 【重要】过河拆桥：生成完图片后，彻底销毁内存中的 WebView 防止内存泄漏
                destroyWebView(backgroundWebView);
            });
        }

        /**
         * 后台测绘并将 WebView 转换为全量长图
         */
        @Nullable
        private Bitmap captureWebView(@NonNull WebView webView) {
            try {
                int width = webView.getWidth();
                // 如果离线测量未就绪，强制兜底一个宽度
                if (width <= 0) width = 1080;

                // 获取 HTML 内容在缩放后的真实物理高度
                float scale = webView.getScale();
                int height = (int) (webView.getHeight() * scale);

                if (height <= 0) height = 2000;

                // 重新让离线 WebView 适配最终测出来的长图宽高
                webView.measure(
                        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                );
                webView.layout(0, 0, width, height);

                // 绘制到画布
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
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