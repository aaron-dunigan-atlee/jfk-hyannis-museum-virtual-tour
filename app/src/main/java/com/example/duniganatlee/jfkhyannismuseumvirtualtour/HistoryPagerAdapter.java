package com.example.duniganatlee.jfkhyannismuseumvirtualtour;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.example.duniganatlee.jfkhyannismuseumvirtualtour.database.HistoryEntry;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.Exhibit;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils.HistoryUtils;

import java.util.List;

/* Pager adapter to allow swiping through the history.
  Based on https://developer.android.com/training/implementing-navigation/lateral.
 */
public class HistoryPagerAdapter extends FragmentStatePagerAdapter {
    private final static String LOG_TAG = "HistoryPagerAdapter";
    private List<HistoryEntry> mHistory;
    public HistoryPagerAdapter(FragmentManager fm, List<HistoryEntry> history, Exhibit[] exhibitList) {
        super(fm);
        mHistory = history;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(LOG_TAG,"Position "+ Integer.toString(position));
        HistoryEntry entry = HistoryUtils.getEntryByPosition(mHistory, position);
        Log.d(LOG_TAG, "Piece " + Integer.toString(entry.getPieceId()));
        Fragment fragment = ViewPagerFragment.newInstance(entry.getPieceId());
        return fragment;
    }

    @Override
    public int getCount() {
        if (mHistory == null) return 0;
        Log.d(LOG_TAG, "History size " + Integer.toString(mHistory.size()));
        return mHistory.size();
    }
}
