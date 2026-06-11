package com.wanderer.journal.auxiliary.interfaces;

import android.text.Annotation;
import android.text.Editable;


public interface EditableFlattenListener {
    /**
     * 获取扁平化后的字符串
     *
     * @param annotation {@link Annotation}对象，储存了富文本的重要数据
     * @param raw        {@link Editable}中的原始字符串
     * @return 扁平化后的字符串
     */
    String getFlattenedText(Annotation annotation, String raw);
}
