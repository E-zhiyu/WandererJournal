package com.wanderer.journal.ui.pages.paragraph.read;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.wanderer.journal.data.save.db.daos.ParagraphDao;

public class ReadViewModelFactory implements ViewModelProvider.Factory {
    private final ParagraphDao dao;

    public ReadViewModelFactory(ParagraphDao dao) {
        this.dao = dao;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ReadViewModel.class)) {
            // 在这里系统终于调用了你那个带参数的构造方法
            return (T) new ReadViewModel(dao);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
