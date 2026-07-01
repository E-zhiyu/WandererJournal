package com.wanderer.journal.helpers.text;

import android.content.Context;
import android.text.Editable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.wanderer.journal.auxiliary.classes.text.RoleRefTextRule;
import com.wanderer.journal.auxiliary.enums.KeyStrings;

import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Locale;

public class ParagraphTextConverter {
    /**
     * 将段落文本扁平化
     *
     * @param editable 输入框中的段落文本
     * @return 扁平化后的字符串
     */
    @NonNull
    @Contract(pure = true)
    public static String flatten(Editable editable) {
        return TextHelper.flattenEditable(
                editable,
                (annotation, raw) -> {
                    String key = annotation.getKey();
                    if (key.equals(KeyStrings.ROLE_ID.getS())) {    //角色引用
                        String roleIdStr = annotation.getValue();
                        String roleName = raw.trim().replace("@", "");
                        return String.format(
                                Locale.getDefault(),
                                "[role_ref:@%s](%s)",
                                roleName, roleIdStr
                        );
                    } else {
                        return "";
                    }
                }
        );
    }

    /**
     * 将段落文本立体化
     *
     * @param context             上下文
     * @param highlightedKeywords 高亮文本
     * @param raw                 扁平化后的段落文本
     * @return 立体化后的富文本，能够直接显示在{@link MaterialTextView}和{@link TextInputEditText}中
     */
    @NonNull
    public static CharSequence hierarchic(Context context, @Nullable List<String> highlightedKeywords, String raw, RoleRefTextRule rule) {
        return TextHelper.hierarchicFromString(
                context,
                highlightedKeywords,
                raw,
                rule
        );
    }
}
