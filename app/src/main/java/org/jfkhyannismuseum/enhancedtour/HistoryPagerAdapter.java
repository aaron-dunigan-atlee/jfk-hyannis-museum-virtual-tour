package org.jfkhyannismuseum.enhancedtour;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import org.jfkhyannismuseum.enhancedtour.database.HistoryEntry;
import org.jfkhyannismuseum.enhancedtour.model.Exhibit;
import org.jfkhyannismuseum.enhancedtour.utils.HistoryUtils;

import java.util.List;

/* Pager adapter to allow swiping through the history.
  Based on https://developer.android.com/training/implementing-navigation/lateral.
 */
public class HistoryPagerAdapter extends FragmentStatePagerAdapter {
    private final static String LOG_TAG = "HistoryPagerAdapter";
    private List<HistoryEntry> mHistory;
    private FragmentManager mFragmentManager;
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
        return mHistory.size();
    }
}
