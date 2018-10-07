package com.example.duniganatlee.jfkhyannismuseumvirtualtour.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface HistoryDao {

    @Query("SELECT * FROM history WHERE exhibitId = :exhibitId")
    LiveData<List<HistoryEntry>> loadExhibitHistory(int exhibitId);

    @Delete
    void removeFromHistory(HistoryEntry historyEntry);

    @Insert
    void addToHistory(HistoryEntry historyEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateHistory(HistoryEntry historyEntry);

    @Query("SELECT * FROM history WHERE pieceId = :pieceId")
    LiveData<HistoryEntry> loadPieceHistory(int pieceId);

    // TODO: Is there a way to replace -1 with a reference to HistoryEntry.NONE?
    @Query("SELECT * FROM history WHERE nextPiece = -1 AND exhibitId = :exhibitId")
    LiveData<HistoryEntry> getLastOnStackForExhibit(int exhibitId);

}
