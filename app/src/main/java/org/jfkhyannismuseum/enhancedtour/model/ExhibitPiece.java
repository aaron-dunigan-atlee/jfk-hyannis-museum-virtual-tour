
package org.jfkhyannismuseum.enhancedtour.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class ExhibitPiece {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("resources")
    @Expose
    private List<ExhibitResource> resources = null;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getDescription() {
        return resources.get(0).getText();
    }

    public String[] getResourceTitles() {
        String titles[] = new String[resources.size()];
        for (int i=0; i<resources.size(); i++) {
            titles[i] = resources.get(i).getTitle();
        }
        return titles;
    }
}
