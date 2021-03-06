package org.jfkhyannismuseum.enhancedtour.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;
// ViewModel for loading the history database.
public class ExhibitHistoryViewModel extends AndroidViewModel {
    private LiveData<List<HistoryEntry>> history;
    private final static String LOG_TAG = "ExhibitHistoryViewModel";
    public ExhibitHistoryViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        history = database.historyDao().loadHistory();
    }

    public LiveData<List<HistoryEntry>> getHistory() { return history; }

}
