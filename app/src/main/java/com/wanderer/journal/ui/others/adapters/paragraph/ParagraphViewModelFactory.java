package com.wanderer.journal.ui.others.adapters.paragraph;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.wanderer.journal.data.save.db.DiaryDatabase;

public class ParagraphViewModelFactory implements ViewModelProvider.Factory {
    private final DiaryDatabase db;     //段落数据库查询器

    /**
     * 指定初始页的构造方法
     *
     * @param db 段落查询接口
     */
    public ParagraphViewModelFactory(DiaryDatabase db) {
        this.db = db;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // 阻塞获取初始位置（因为这是在后台实例化的）
        if (modelClass.isAssignableFrom(ParagraphViewModel.class)) {
            // 在这里系统终于调用了你那个带参数的构造方法
            return (T) new ParagraphViewModel(db);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
