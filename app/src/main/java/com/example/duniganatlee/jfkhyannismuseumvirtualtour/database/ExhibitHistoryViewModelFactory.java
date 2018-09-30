package com.example.duniganatlee.jfkhyannismuseumvirtualtour.database;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

/* ViewModelFactory for retrieving an exhibit viewing history from the database.
 * ViewModelFactory is needed because we need to pass an exhibit id parameter to the constructor.
 * Modeled after Exercise T09b.10 from the Udacity course.*/
public class ExhibitHistoryViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppDatabase mHistoryDb;
    private final int mExhibitId;
    public ExhibitHistoryViewModelFactory(AppDatabase database, int exhibitId) {
        mHistoryDb = database;
        mExhibitId = exhibitId;
    }
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new ExhibitHistoryViewModel(mHistoryDb, mExhibitId);
    }


}
