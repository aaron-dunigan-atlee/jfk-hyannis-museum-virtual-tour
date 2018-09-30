package com.example.duniganatlee.jfkhyannismuseumvirtualtour.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import java.util.List;
// ViewModel for loading the history database for an exhibit.
public class ExhibitHistoryViewModel extends ViewModel {
    private LiveData<List<HistoryEntry>> exhibitHistory;
    public ExhibitHistoryViewModel(AppDatabase database, int exhibitId) {
        exhibitHistory = database.historyDao().loadExhibitHistory(exhibitId);
    }

    public LiveData<List<HistoryEntry>> getExhibitHistory() {
        return exhibitHistory;
    }
}
