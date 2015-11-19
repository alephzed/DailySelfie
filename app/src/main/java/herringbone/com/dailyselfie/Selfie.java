package herringbone.com.dailyselfie;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Date;

public class Selfie implements Serializable{
    Integer id;
    String filename;
    Integer charcoal;
    Integer gaussian;
    Bitmap thumbnail;
    String username;
    Date recordDate;
    String description;
    String processedFilename;

    public Selfie() {

    }

    public Selfie(String filename, Integer charcoal, Integer gaussian, Bitmap thumbnail, String username,
                  Date recordDate, String description, String processedFilename) {
        this.filename = filename;
        this.charcoal = charcoal;
        this.gaussian = gaussian;
        this.thumbnail = thumbnail;
        this.username = username;
        this.recordDate = recordDate;
        this.description = description;
        this.processedFilename = processedFilename;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getCharcoal() {
        return charcoal;
    }

    public void setCharcoal(Integer charcoal) {
        this.charcoal = charcoal;
    }

    public Integer getGaussian() {
        return gaussian;
    }

    public void setGaussian(Integer gaussian) {
        this.gaussian = gaussian;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProcessedFilename() {
        return processedFilename;
    }

    public void setProcessedFilename(String processedFilename) {
        this.processedFilename = processedFilename;
    }

    public boolean equals(Object other) {
        Selfie otherSelfie = (Selfie)other;
        if (otherSelfie.getFilename().equals(this.getFilename())) {
            return true;
        }
        return false;
    }

}
