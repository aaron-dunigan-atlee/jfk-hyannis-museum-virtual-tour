
package com.example.duniganatlee.jfkhyannismuseumvirtualtour.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// Thanks to http://www.jsonschema2pojo.org/ for making this easy.
public class Exhibit {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("exhibit_title")
    @Expose
    private String exhibitTitle;
    @SerializedName("exhibit_pieces")
    @Expose
    private List<ExhibitPiece> exhibitPieces = null;

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getExhibitTitle() {
        return exhibitTitle;
    }

    public void setExhibitTitle(String exhibitTitle) {
        this.exhibitTitle = exhibitTitle;
    }

    public List<ExhibitPiece> getExhibitPieces() {
        return exhibitPieces;
    }

    public void setExhibitPieces(List<ExhibitPiece> exhibitPieces) {
        this.exhibitPieces = exhibitPieces;
    }
    
    public static Exhibit getExhibitById(Exhibit[] exhibitList, int id) {
        for (Exhibit exhibit : exhibitList) {
            if (exhibit.getId() == id) {
                return exhibit;
            }
        }
        return null;
    }

    public ExhibitPiece getPieceById(int id) {
        for (ExhibitPiece piece : exhibitPieces) {
            if (piece.getId() == id) {
                return piece;
            }
        }
        return null;
    }

    public static ExhibitPiece getPieceById(Exhibit[] exhibits, int id) {
        int exhibitId = getExhibitId(id);
        Exhibit exhibit = getExhibitById(exhibits, exhibitId);
        if (exhibit != null) {
            for (ExhibitPiece piece : exhibit.getExhibitPieces()) {
                if (piece.getId() == id) {
                    return piece;
                }
            }
        }
        return null;
    }

    public static int getExhibitId(int pieceId) {
        return pieceId / 1000;
    }
}
