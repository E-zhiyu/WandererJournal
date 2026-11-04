package com.wanderer.journal.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.wanderer.journal.auxiliary.enums.unique.LogTags;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class LocationHelper {
    static class LocationRequestListener implements LocationListener {
        private final int targetIndex;                      //缓冲区的目标的下标
        private final Location[] locationBuffer;            //外部传递的位置对象缓冲区
        private final Function<Location, Void> successFun;  //位置更新回调
        private final Runnable clearAction;                 //清理方法
        private final Function<String, Location> providerDisabledFun;   //提供者被禁用的回调

        /**
         * @param targetIndex         修改的缓冲区的下标
         * @param locationBuffer      位置缓冲区
         * @param updateFun           位置更新回调
         * @param clearAction         清理方法
         * @param providerDisabledFun 提供者被禁用的回调
         */
        LocationRequestListener(int targetIndex, Location[] locationBuffer, Function<Location, Void> updateFun, Function<String, Location> providerDisabledFun, Runnable clearAction) {
            this.targetIndex = targetIndex;
            this.locationBuffer = locationBuffer;
            this.successFun = updateFun;
            this.clearAction = clearAction;
            this.providerDisabledFun = providerDisabledFun;
        }

        @Override
        public void onLocationChanged(@NonNull Location location) {
            locationBuffer[targetIndex] = location;
            successFun.apply(location);
            clearAction.run();
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            clearAction.run();
            Log.w(LogTags.LOCATION_HELPER.n(), provider + "关闭，但是没有获取到有效位置，改为获取最近已知位置");
            Location lastLocation = providerDisabledFun.apply(provider);
            if (lastLocation != null) {
                locationBuffer[targetIndex] = lastLocation;
                Log.i(LogTags.LOCATION_HELPER.n(), provider + "已获取到最近已知位置");
            } else {
                Log.w(LogTags.LOCATION_HELPER.n(), provider + "无法获取最近已知位置");
            }
        }
    }

    public interface OnceLocationListener {
        /**
         * 位置获取失败回调
         *
         * @param message 失败原因
         */
        void onFailed(String message);

        /**
         * 位置获取成功回调
         *
         * @param location 成功原因
         */
        void onSucceed(Location location);
    }

    /**
     * 获取缓存的定位结果
     *
     * @param context 上下文
     */
    public static void getLocation(@NonNull Context context, @NonNull OnceLocationListener listener) {
        //获取定位服务
        Log.d(LogTags.LOCATION_HELPER.n(), "开始获取当前位置");
        LocationManager locationManager = context.getSystemService(LocationManager.class);
        if (locationManager == null) {
            Log.e(LogTags.LOCATION_HELPER.n(), "无法获取LocationManager");
            listener.onFailed("位置服务初始化异常");
            return;
        }

        //检查权限
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LogTags.LOCATION_HELPER.n(), "没有定位权限");
            listener.onFailed("请授予定位权限");
            return;
        }

        // 检查手机的 GPS 或网络定位开关是否开启
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled) {
            Log.e(LogTags.LOCATION_HELPER.n(), "定位服务开关未启用");
            listener.onFailed("定位服务开关未启用");
            return;
        }

        //设置超时机制
        final int GPS_INDEX = 0;
        final int NET_INDEX = 1;
        final Location[] LOCATION_BUFFER = {null, null};
        List<LocationListener> listenerList = new ArrayList<>();
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable timeoutRun = () -> {
            Log.w(LogTags.LOCATION_HELPER.n(), "触发超时机制");
            removeLocationRequests(locationManager, listenerList);

            for (Location location : LOCATION_BUFFER) {
                if (location != null) {
                    listener.onSucceed(location);
                    Log.i(LogTags.LOCATION_HELPER.n(), "超时后有可用位置" + location);
                    return;
                }
            }

            Log.e(LogTags.LOCATION_HELPER.n(), "超时后没有可用位置");
            listener.onFailed("无法获取位置，请重试或关闭定位功能");
        };
        handler.postDelayed(timeoutRun, 1500);

        //实例化位置更新监听
        LocationRequestListener gpsListener = new LocationRequestListener(
                GPS_INDEX,
                LOCATION_BUFFER,
                location -> {
                    //移除超时计时器
                    handler.removeCallbacks(timeoutRun);

                    Log.i(LogTags.LOCATION_HELPER.n(), "位置成功更新" + location);
                    listener.onSucceed(location);
                    return null;
                },
                provider -> getLastKnownLocation(context, provider),
                () -> removeLocationRequests(locationManager, listenerList)
        );
        listenerList.add(gpsListener);
        LocationRequestListener netListener = new LocationRequestListener(
                NET_INDEX,
                LOCATION_BUFFER,
                location -> {
                    //移除超时计时器
                    handler.removeCallbacks(timeoutRun);

                    Log.i(LogTags.LOCATION_HELPER.n(), "位置成功更新" + location);
                    listener.onSucceed(location);
                    return null;
                },
                provider -> getLastKnownLocation(context, provider),
                () -> removeLocationRequests(locationManager, listenerList)
        );
        listenerList.add(netListener);

        //同时监听 GPS 和网络的位置
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                200,
                1.0f,
                gpsListener
        );
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                200,
                1.0f,
                gpsListener
        );
    }

    /**
     * 取消位置监听
     *
     * @param manager      {@link LocationManager}实例
     * @param listenerList 需要取消监听的监听器
     */
    private static void removeLocationRequests(LocationManager manager, @NonNull List<LocationListener> listenerList) {
        for (int i = 0; i < listenerList.size(); i++) {
            LocationListener savedListener = listenerList.get(i);
            if (savedListener != null) {
                manager.removeUpdates(savedListener);
                listenerList.set(i, null);
            }
        }
    }

    /**
     * 获取上次已知的位置
     *
     * @param context  上下文
     * @param provider 位置提供者
     * @return 上次已知的位置
     */
    @Nullable
    public static Location getLastKnownLocation(@NonNull Context context, String provider) {
        //获取服务
        LocationManager locationManager = context.getSystemService(LocationManager.class);
        if (locationManager == null) {
            Log.e(LogTags.LOCATION_HELPER.n(), "无法获取LocationManager");
            return null;
        }

        //权限检查
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LogTags.LOCATION_HELPER.n(), provider + "没有定位权限，无法获取最近已知位置");
            return null;
        }

        //获取定位
        Location location = locationManager.getLastKnownLocation(provider);
        Log.i(LogTags.LOCATION_HELPER.n(), location == null ? "无法获取上次的位置" : "获取到上次的位置" + location);
        return location;
    }
}
