package com.wanderer.journal.ui.others.adapters.paragraph;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.wanderer.journal.data.save.db.DiaryDatabase;

import java.time.LocalDate;

public class ParagraphViewModelFactory implements ViewModelProvider.Factory {
    private final DiaryDatabase db;     //段落数据库查询器
    private final LocalDate start, end; //起止日期，用于截取段落

    /**
     * 指定初始位置并且指定日期范围的构造方法
     *
     * @param db          段落查询接口
     * @param start        起始日期（包含）
     * @param end          结束日期（不包含）
     */
    public ParagraphViewModelFactory(DiaryDatabase db, LocalDate start, LocalDate end) {
        this.db = db;
        this.start = start;
        this.end = end;
    }

    /**
     * 指定初始页页但不指定日期范围的构造方法
     *
     * @param db          段落查询接口
     */
    public ParagraphViewModelFactory(DiaryDatabase db) {
        this(db, null, null);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // 阻塞获取初始位置（因为这是在后台实例化的）
        if (modelClass.isAssignableFrom(ParagraphViewModel.class)) {
            // 在这里系统终于调用了你那个带参数的构造方法
            return (T) new ParagraphViewModel(db, start, end);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
