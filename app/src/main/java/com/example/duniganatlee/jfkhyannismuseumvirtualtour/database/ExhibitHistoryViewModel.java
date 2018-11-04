package com.example.duniganatlee.jfkhyannismuseumvirtualtour.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;
// ViewModel for loading the history database.
public class ExhibitHistoryViewModel extends AndroidViewModel {
    private LiveData<List<HistoryEntry>> history;
    private final MutableLiveData<List<HistoryEntry>> historyListForConfigChange = new MutableLiveData<List<HistoryEntry>>();
    private final static String LOG_TAG = "ExhibitHistoryViewModel";
    public ExhibitHistoryViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        history = database.historyDao().loadHistory();
    }

    public LiveData<List<HistoryEntry>> getHistory() {
        if (history == null) {
            AppDatabase database = AppDatabase.getInstance(this.getApplication());
            history = database.historyDao().loadHistory();
        }
            return history;
    }

    public void saveHistoryForConfigurationChange(List<HistoryEntry> historyEntries) {
        Log.d(LOG_TAG, "Saving history.");
        historyListForConfigChange.setValue(historyEntries);
    }

    public List<HistoryEntry> getHistoryForConfigurationChange() {
        Log.d(LOG_TAG, "Restoring history.");
        return historyListForConfigChange.getValue();
    }

}
