package com.wanderer.journal.ui.pages.life_note;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.wanderer.journal.data.save.db.entities.LifeNoteEntity;

public class LifeNoteInputViewModel extends ViewModel {
    private final MutableLiveData<LifeNoteEntity> lifeNoteModelLiveData = new MutableLiveData<>();

    public MutableLiveData<LifeNoteEntity> getLifeNoteModelLiveData() {
        return lifeNoteModelLiveData;
    }

    @Nullable
    public LifeNoteEntity getLifeNoteModel() {
        return lifeNoteModelLiveData.getValue();
    }

    public void setLifeNoteModel(LifeNoteEntity lifeNote) {
        lifeNoteModelLiveData.setValue(lifeNote);
    }
}
