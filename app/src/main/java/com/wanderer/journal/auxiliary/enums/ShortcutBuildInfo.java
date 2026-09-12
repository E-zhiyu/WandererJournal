package com.wanderer.journal.auxiliary.enums;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.wanderer.journal.R;
import com.wanderer.journal.ui.pages.DiaryReadActivity;
import com.wanderer.journal.ui.pages.WriteActivity;

import java.util.function.Function;

public enum ShortcutBuildInfo {
    WRITE_DIARY(
            R.string.write_diary,
            "write_diary",
            R.drawable.outline_ink_pen_24,
            context -> {
                Intent intent = new Intent(context, WriteActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                return intent;
            },
            true
    ),
    READ_DIARY(
            R.string.read_diary,
            "read_diary",
            R.drawable.outline_undereye_24,
            context -> {
                Intent intent = new Intent(context, DiaryReadActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                return intent;
            },
            true
    );

    @StringRes
    private final int shortLabelRes;
    private final String id;
    @DrawableRes
    private final int iconRes;
    private final Function<Context, Intent> intentBuilder;
    private final boolean includeParent;

    ShortcutBuildInfo(int shortLabelRes, String id, int iconRes, Function<Context, Intent> intentBuilder, boolean includeParent) {
        this.shortLabelRes = shortLabelRes;
        this.id = id;
        this.iconRes = iconRes;
        this.intentBuilder = intentBuilder;
        this.includeParent = includeParent;
    }

    public int getShortLabelRes() {
        return shortLabelRes;
    }

    public String getId() {
        return id;
    }

    public int getIconRes() {
        return iconRes;
    }

    /**
     * 获取{@link Intent}对象
     *
     * @param context 上下文
     * @return 点击快捷方式后触发的{@link Intent}
     */
    public Intent getIntent(Context context) {
        return intentBuilder.apply(context);
    }

    public boolean isIncludeParent() {
        return includeParent;
    }
}
