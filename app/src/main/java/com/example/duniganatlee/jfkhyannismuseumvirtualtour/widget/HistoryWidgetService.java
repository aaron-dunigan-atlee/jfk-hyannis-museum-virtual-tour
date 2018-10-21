package com.example.duniganatlee.jfkhyannismuseumvirtualtour.widget;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.duniganatlee.jfkhyannismuseumvirtualtour.MainActivity;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.R;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.database.AppDatabase;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.database.HistoryEntry;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.Exhibit;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils.JsonUtils;

import java.util.List;

public class HistoryWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new HistoryRemoteViewsFactory(this.getApplicationContext());
    }
}

class HistoryRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private List<HistoryEntry> mHistory;
    private String mExhibitsJson ;
    private Exhibit[] mExhibitsList;
    private static final String WIDGET_LOG_TAG = "WidgetService";

    public HistoryRemoteViewsFactory(Context applicationContext) {
        mContext = applicationContext;
        // TODO: Handle error loading JSON.
        mExhibitsJson = JsonUtils.loadJSONFromAsset(mContext);
        mExhibitsList = JsonUtils.parseExhibitList(mExhibitsJson);
    }

    @Override
    public void onCreate() {
        // onDataSetChanged();
    }
    // Called when notifyAppWidgetViewDataChanged is called.
    @Override
    public void onDataSetChanged() {
        /*
        Update mHistory by accessing the database.  According to
        https://developer.android.com/reference/android/widget/RemoteViewsService.RemoteViewsFactory#onDataSetChanged(),
        "expensive tasks can be safely performed synchronously within this method.
        In the interim, the old data will be displayed within the widget."
        Therefore, no need to wrap this in an AsyncTask or AppExecutor.
        */
        AppDatabase historyDb = AppDatabase.getInstance(mContext);
        List<HistoryEntry> history = historyDb.historyDao().loadHistoryList();

        if(history != null){
            Log.d(WIDGET_LOG_TAG, "Accessed db successfully.");
            mHistory = history;
        } else {
            Log.d(WIDGET_LOG_TAG, "Couldn't access db");
        }
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        if (mHistory == null) return 0;
        return mHistory.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        /* Get the HistoryEntry at position, and use its pieceId to look
           up the corresponding ExhibitPiece so we can get its title.
         */
        if (mHistory == null || mHistory.size() == 0) { return null; }
        HistoryEntry historyEntry = mHistory.get(position);
        int exhibitId = historyEntry.getExhibitId();
        int pieceId = historyEntry.getPieceId();
        Exhibit exhibit = Exhibit.getExhibitById(mExhibitsList, exhibitId);
        if (exhibit == null) return null;
        String exhibitPieceTitle = exhibit.getPieceById(pieceId).getTitle();
        // Set the title in the ListView
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_history_list_item);
        views.setTextViewText(R.id.widget_text_history_item, exhibitPieceTitle);

        // Set onclick intent using a fillInIntent which interprets the PendingIntentTemplate
        // which was set in MuseumHistoryWidgetProvider onCreate().
        // Specifically, specify which piece to open in Main Activity by sending its position in the history.
        // TODO: Currently, this intent opens a new instance of the MainActivity, moving the current instance to the back stack.
        // How do I make this so it opens in the current instance, or at least removes the current instance from the back stack?
        // I don't need multiple instances of MainActivity sucking up resources, since my app handles history internally.
        // Is this related to LuanchMode (singleTop, etc.)?
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(MainActivity.HISTORY_POSITION, position);
        views.setOnClickFillInIntent(R.id.widget_text_history_item, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
