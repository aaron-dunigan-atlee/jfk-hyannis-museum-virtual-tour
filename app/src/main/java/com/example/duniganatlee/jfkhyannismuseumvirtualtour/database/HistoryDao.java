package com.example.duniganatlee.jfkhyannismuseumvirtualtour.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.database.Cursor;

import java.util.List;

@Dao
public interface HistoryDao {

    @Query("SELECT * FROM history")
    LiveData<List<HistoryEntry>> loadHistory();

    @Delete
    void removeFromHistory(HistoryEntry historyEntry);

    // "INSERT or UPDATE" command.  Updates if the entry exists; else inserts it.
    // See https://stackoverflow.com/a/45677347/10332984
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateOrAddToHistory(HistoryEntry historyEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateOrAddToHistory(List<HistoryEntry> historyEntries);

    @Insert
    void addToHistory(HistoryEntry historyEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateHistory(HistoryEntry historyEntry);

    @Query("SELECT * FROM history WHERE pieceId = :pieceId")
    LiveData<HistoryEntry> loadPieceHistory(int pieceId);

    // TODO: Is there a way to replace -1 with a reference to HistoryEntry.NONE?
    @Query("SELECT * FROM history WHERE nextPiece = -1")
    LiveData<HistoryEntry> getLastOnStack();

    // Needed for widget to access database?
    // See https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/CheeseDao.java
    // and https://stackoverflow.com/questions/46804775/room-persistence-library-and-content-provider
    @Query("SELECT * FROM history")
    List<HistoryEntry> loadHistoryList();

}
