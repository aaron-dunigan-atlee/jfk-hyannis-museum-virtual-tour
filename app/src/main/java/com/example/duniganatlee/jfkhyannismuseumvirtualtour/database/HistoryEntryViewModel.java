package com.example.duniganatlee.jfkhyannismuseumvirtualtour.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

public class HistoryEntryViewModel extends ViewModel {
    private LiveData<HistoryEntry> historyEntry;
    public HistoryEntryViewModel(AppDatabase database, int pieceId) {
        historyEntry = database.historyDao().loadPieceHistory(pieceId);
    }

    public LiveData<HistoryEntry> getExhibitHistory() {
        return historyEntry;
    }

}
