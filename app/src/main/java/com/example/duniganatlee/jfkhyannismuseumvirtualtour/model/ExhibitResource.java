
package com.example.duniganatlee.jfkhyannismuseumvirtualtour.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ExhibitResource {

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("resource_URL")
    @Expose
    private String resourceURL;
    @SerializedName("background_image_URL")
    @Expose
    private String backgroundImageURL;
    @SerializedName("resource_type")
    @Expose
    private String resourceType;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResourceURL() {
        return resourceURL;
    }

    public void setResourceURL(String resourceURL) {
        this.resourceURL = resourceURL;
    }

    public String getBackgroundImageURL() {
        return backgroundImageURL;
    }

    public void setBackgroundImageURL(String backgroundImageURL) {
        this.backgroundImageURL = backgroundImageURL;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

}
