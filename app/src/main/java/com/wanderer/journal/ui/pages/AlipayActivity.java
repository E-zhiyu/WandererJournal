package com.wanderer.journal.ui.pages;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alipay.sdk.app.EnvUtils;
import com.alipay.sdk.app.PayTask;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.auxiliary.classes.PayResult;
import com.wanderer.journal.databinding.ActivityAlipayBinding;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AlipayActivity extends AppCompatActivity {
    private ActivityAlipayBinding binding;
    private static final String TAG = "AlipayDemo";
    private static final int SDK_PAY_FLAG = 1;
    private final String ORDER_URL = "https://frp-fun.com:18558/alipay/createOrder?subject=AndroidTest&totalAmount=0.01";

    // 接收子线程支付宝返回结果的 Handler
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == SDK_PAY_FLAG) {
                // 支付宝返回的同步结果
                PayResult payResult = new PayResult((Map<String, String>) msg.obj);

                String resultInfo = payResult.getResult(); // 同步返回的结果
                String resultStatus = payResult.getResultStatus(); // 状态码

                // 判断 resultStatus 状态码
                // 9000 代表订单支付成功
                if (TextUtils.equals(resultStatus, "9000")) {
                    new MaterialAlertDialogBuilder(AlipayActivity.this)
                            .setTitle("沙箱支付")
                            .setMessage("支付成功")
                            .setNegativeButton("关闭", (dialogInterface, i) -> finish())
                            .show();
                    // 【注意】这里只是客户端的同步趋向成功，实际业务请以 Windows 服务器收到的异步通知为准
                } else if (TextUtils.equals(resultStatus, "6001")) {
                    // 6001 代表用户中途取消了支付
                    Toast.makeText(AlipayActivity.this, "支付已取消", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // 8000 代表正在处理中，4000 代表支付失败等
                    Toast.makeText(AlipayActivity.this, "支付失败，错误码: " + resultStatus, Toast.LENGTH_SHORT).show();
                    finish();
                }
                Log.d(TAG, "支付宝返回状态码: " + resultStatus + "，详情: " + resultInfo);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlipayBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.getRoot().post(this::getOrderStringFromServer);
    }

    /**
     * 从 Java 后端服务器获取加密订单字符串
     */
    private void getOrderStringFromServer() {
        OkHttpClient client = getUnsafeOkHttpClient();
        Request request = new Request.Builder().url(ORDER_URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "请求服务器失败: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(AlipayActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    final String orderString = response.body().string();
                    Log.d(TAG, "从后端拿到的 OrderString: " + orderString);

                    if (!TextUtils.isEmpty(orderString) && !orderString.contains("失败")) {
                        // 第二步：必须在新线程中调用支付宝 SDK 唤起钱包
                        startAlipayThread(orderString);
                    } else {
                        runOnUiThread(() -> Toast.makeText(AlipayActivity.this, "后端生成订单失败", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    @NonNull
    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true); // 信任所有域名

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 核心方法：异步调起支付宝 SDK
     */
    private void startAlipayThread(final String orderString) {
        // 关键代码：开启支付宝 SDK 的沙箱/测试环境模式
        // 0 代表正式环境（默认），1 代表沙箱测试环境
        EnvUtils.setEnv(com.alipay.sdk.app.EnvUtils.EnvEnum.SANDBOX);

        final Runnable payRunnable = () -> {
            // 创建 PayTask 对象
            PayTask alipay = new PayTask(AlipayActivity.this);
            // 调用支付接口，该方法会阻塞线程，直至用户在支付宝中支付完成或退出
            // 第二个参数如果是 true，则在没安装支付宝 App 时会弹出加载 H5 网页支付的 Loading
            Map<String, String> result = alipay.payV2(orderString, true);

            Log.i(TAG, "支付宝同步返回原始结果: " + result.toString());

            // 将结果发送给主线程的 Handler 处理
            Message msg = new Message();
            msg.what = SDK_PAY_FLAG;
            msg.obj = result;
            mHandler.sendMessage(msg);
        };

        // 启动子线程
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }
}