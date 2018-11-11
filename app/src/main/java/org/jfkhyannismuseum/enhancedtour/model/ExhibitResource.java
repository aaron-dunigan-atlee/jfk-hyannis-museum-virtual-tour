
package org.jfkhyannismuseum.enhancedtour.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ExhibitResource {
    static public final String IMAGE = "image";
    static public final String AUDIO = "audio";
    static public final String VIDEO = "video";

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("resource_URL")
    @Expose
    private String resourceURL;
    @SerializedName("background_image_URL")
    @Expose
    private String backgroundImageURL;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("text")
    @Expose
    private String text;

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

    public String getType() {
        return type;
    }

    public void setType(String resourceType) {
        this.type = resourceType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
