package com.wanderer.journal.helpers.permission;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hjq.permissions.OnPermissionDescription;
import com.hjq.permissions.permission.base.IPermission;

import java.util.List;
import java.util.function.Function;

public class PermissionDialogDescription implements OnPermissionDescription {
    private final Function<List<IPermission>, String> descriptionFunction;

    public PermissionDialogDescription(Function<List<IPermission>, String> descriptionFunction) {
        this.descriptionFunction = descriptionFunction;
    }

    @Override
    public void askWhetherRequestPermission(@NonNull Activity activity, @NonNull List<IPermission> requestList, @NonNull Runnable continueRequestRunnable, @NonNull Runnable breakRequestRunnable) {
        String description = descriptionFunction.apply(requestList);
        new MaterialAlertDialogBuilder(activity)
                .setTitle("权限申请")
                .setMessage(description.isEmpty() ? "请授予权限。" : description)
                .setPositiveButton("去申请", (dialog, which) -> continueRequestRunnable.run())
                .setNegativeButton("取消", (dialog, which) -> breakRequestRunnable.run())
                .show();
    }

    @Override
    public void onRequestPermissionStart(@NonNull Activity activity, @NonNull List<IPermission> requestList) {

    }

    @Override
    public void onRequestPermissionEnd(@NonNull Activity activity, @NonNull List<IPermission> requestList) {

    }
}
