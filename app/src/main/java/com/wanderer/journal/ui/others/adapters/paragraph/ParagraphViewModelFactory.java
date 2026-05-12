package com.wanderer.journal.ui.others.adapters.paragraph;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.wanderer.journal.data.save.db.daos.ParagraphDao;

import java.time.LocalDate;

public class ParagraphViewModelFactory implements ViewModelProvider.Factory {
    private final ParagraphDao dao;     //段落数据库查询器
    private final LocalDate start, end; //起止日期，用于截取段落

    public ParagraphViewModelFactory(ParagraphDao dao, LocalDate start, LocalDate end) {
        this.dao = dao;
        this.start = start;
        this.end = end;
    }

    public ParagraphViewModelFactory(ParagraphDao dao) {
        this(dao, null, null);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ParagraphViewModel.class)) {
            // 在这里系统终于调用了你那个带参数的构造方法
            return (T) new ParagraphViewModel(dao, start, end);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
