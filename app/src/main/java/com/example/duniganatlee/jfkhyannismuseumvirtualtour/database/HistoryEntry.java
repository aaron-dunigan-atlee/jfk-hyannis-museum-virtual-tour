package com.example.duniganatlee.jfkhyannismuseumvirtualtour.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

// POJO for tracking the exhibit viewing history.
// History will be maintained like a linked list: each entry records a pieceId, and the id of
// the next and previous piece viewed.  These are NONE if there is no next or no previous.
@Entity(tableName = "history")
public class HistoryEntry {
    @Ignore
    public static final int NONE = -1;

    // Note that piece id's must be unique.
    // Convention will be that piece id begins with its exhibit's id,
    // followed by 3 digits identifying the piece within its exhibit.
    @PrimaryKey(autoGenerate = false)
    private int pieceId;
    private int exhibitId;
    private int nextPiece = NONE;
    private int previousPiece = NONE;

    public HistoryEntry(int pieceId, int exhibitId, int previousPiece, int nextPiece) {
        setExhibitId(exhibitId);
        setNextPiece(nextPiece);
        setPieceId(pieceId);
        setPreviousPiece(previousPiece);
    }

    public int getPieceId() {
        return pieceId;
    }

    public void setPieceId(int pieceId) {
        this.pieceId = pieceId;
    }

    public int getExhibitId() {
        return exhibitId;
    }

    public void setExhibitId(int exhibitId) {
        this.exhibitId = exhibitId;
    }

    public int getNextPiece() {
        return nextPiece;
    }

    public void setNextPiece(int nextPiece) {
        this.nextPiece = nextPiece;
    }

    public int getPreviousPiece() {
        return previousPiece;
    }

    public void setPreviousPiece(int previousPiece) {
        this.previousPiece = previousPiece;
    }
}
