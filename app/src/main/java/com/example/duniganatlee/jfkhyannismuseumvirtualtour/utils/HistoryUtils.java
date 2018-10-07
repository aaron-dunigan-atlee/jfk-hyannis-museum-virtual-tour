package com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils;

import com.example.duniganatlee.jfkhyannismuseumvirtualtour.database.HistoryEntry;

import java.util.List;

public class HistoryUtils {
    // Get the final entry (the last one in the stack) for a history list.
    public static HistoryEntry getFinalEntry(List<HistoryEntry> history) {
        if (history.size() == 0) { return null; }
        for (HistoryEntry entry : history) {
            if (entry.getNextPiece() == HistoryEntry.NONE) {
                return entry;
            }
        }
        return null;
    }

    public static HistoryEntry getEntryById(List<HistoryEntry> history, int pieceId) {
        for (HistoryEntry entry : history) {
            if (entry.getPieceId() == pieceId) {
                return entry;
            }
        }
        return null;
    }
}
