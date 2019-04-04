package com.mobilewebcam2.mobilewebcam2.managers;

import android.graphics.Bitmap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Handles all the kinds of storage of the pictures: in the memory/SD, over FTP, or
 * by sharing them, according to user settings.
 *
 * Other parts of the application should be unaware of how the pictures are stored.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "Storage Type", include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = LocalStorageManager.class, name = StorageManager.STORAGE_LOCAL),
        //@JsonSubTypes.Type(value = Bar.class, name = "bar")
})
public abstract class StorageManager  extends MWCSettings {

    /**
     * Tag for the logger. Every class should have one.
     */
    //@Expose(serialize = false, deserialize = false)
    private static final String LOG_TAG = "StorageManager";

    /**
     * Internal Settings class, to be serialized.
     */
    @JsonProperty("Settings")
    private final InternalSettings internalSettings;

    // Jackson has trouble with non static inner classes
    // http://cowtowncoder.com/blog/archives/2010/08/entry_411.html
    @JsonPropertyOrder(alphabetic=true)
    static private class InternalSettings {

        @JsonProperty("Storage Type")
        private final String storageTypeName;
        @JsonProperty("Add the timestamp to the picture name?")
        private final boolean addTimestamp;
        @JsonProperty("Add the timestap to the beginning or the end of the name?")
        private boolean timestampAtTheBeginning; // True = Beginning, False = End
        @JsonProperty("Format string for the timestamp: ")
        private String timestampFormatString;

        protected InternalSettings(String storageTypeName){
            this.storageTypeName = storageTypeName;
            addTimestamp = true;
            timestampAtTheBeginning = false;
            timestampFormatString = "";
        }
    }

    /**
     * Utility constants for the storageName value
     */
    public static final String STORAGE_LOCAL = "Local Storage (Save on disk)";
    public static final String STORAGE_FTP = "FTP Storage (Upload)";
    public static final String STORAGE_SOCIALMEDIA = "Social Media Storage (Sharing)";

    protected StorageManager(String storageTypeName){
        internalSettings = new InternalSettings(storageTypeName);
    }

    public String getStorageTypeName() { return internalSettings.storageTypeName; }

    protected boolean getAddTimestamp() { return internalSettings.addTimestamp; }

    protected boolean getTimmestampAtTheBeginning() { return internalSettings.timestampAtTheBeginning; }

    protected String getTimestampFormatString() { return internalSettings.timestampFormatString; }

    public abstract void storePicture(Bitmap bitmap);


    @Override
    public String toString(){
        String repr = "";
        repr += "\t\tStorage Type: " + internalSettings.storageTypeName + "\n";
        repr += "\t\tAdd timestamp to picture name? " + internalSettings.addTimestamp + "\n";
        repr += "\t\tAdd timestamp at the beginning of the picture name (if false, at the end)? "
                        + internalSettings.timestampAtTheBeginning + "\n";
        repr += "\t\tTimestamp format string: " + internalSettings.timestampFormatString + "\n";

        return repr;
    }
}