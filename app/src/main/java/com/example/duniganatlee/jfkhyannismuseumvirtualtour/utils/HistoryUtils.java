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

    // Get the first entry in the history stack.
    public static HistoryEntry getFirstEntry(List<HistoryEntry> history) {
        if (history.size() == 0) { return null; }
        for (HistoryEntry entry : history) {
            if (entry.getPreviousPiece() == HistoryEntry.NONE) {
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

    public static HistoryEntry getEntryByPosition(List<HistoryEntry> history, int position) {
        HistoryEntry entry = getFirstEntry(history);
        for (int i=0; i<position; i++) {
            entry = getEntryById(history, entry.getNextPiece());
        }
        return entry;
    }
}
