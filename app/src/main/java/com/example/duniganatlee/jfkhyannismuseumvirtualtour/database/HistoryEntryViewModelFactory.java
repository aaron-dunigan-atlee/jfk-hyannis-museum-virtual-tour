package com.example.duniganatlee.jfkhyannismuseumvirtualtour.database;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

public class HistoryEntryViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppDatabase mHistoryDb;
    private final int mPieceId;
    public HistoryEntryViewModelFactory(AppDatabase database, int pieceId) {
        mHistoryDb = database;
        mPieceId = pieceId;
    }
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new HistoryEntryViewModel(mHistoryDb, mPieceId);
    }
}
