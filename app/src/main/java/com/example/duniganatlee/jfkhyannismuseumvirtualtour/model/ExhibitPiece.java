
package com.example.duniganatlee.jfkhyannismuseumvirtualtour.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ExhibitPiece {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("narrationURL")
    @Expose
    private String narrationURL;

    @SerializedName("resources")
    @Expose
    private List<ExhibitResource> resources = null;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNarrationURL() {
        return narrationURL;
    }

    public void setNarrationURL(String narrationURL) {
        this.narrationURL = narrationURL;
    }

    public List<ExhibitResource> getResources() {
        return resources;
    }

    public void setResources(List<ExhibitResource> resources) {
        this.resources = resources;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
