package com.mobilewebcam2.mobilewebcam2.managers;

import android.util.Log;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.mobilewebcam2.mobilewebcam2.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * The head of the Manager's hierarchy. Singleton class.
 */
@JsonFilter("Only Settings")
public class RootManager extends MWCSettings{

    /**
     * Tag for the logger. Every class should have one.
     */
    private static final String LOG_TAG = "RootManager";

    /**
     * Internal Settings class, to be serialized.
     */
    @JsonProperty("Settings")
    private InternalSettings internalSettings = new InternalSettings();

    private transient ObjectMapper objectMapper;
    private transient FilterProvider serializationFilters;


    private RootManager() {
        /*
         * Setup Jackson's ObjectMapper
         */
        objectMapper = new ObjectMapper();
        // Turns off autodetection of properties based on getters and setters
        objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        // Serialize also private and protected fields.
        objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        // Specifies that only the "Settings" field should be serialized
        // FIXME this does not really work somehow
        serializationFilters = new SimpleFilterProvider()
                .addFilter("Only Settings",
                        SimpleBeanPropertyFilter.filterOutAllExcept("Settings"));
        // If a class has no properties, don't fail
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }


    // https://www.journaldev.com/1377/java-singleton-design-pattern-best-practices-examples
    private static class SingletonHelper{
        private static final RootManager INSTANCE = new RootManager();
    }

    /**
     * Returns the singleton instance of RootManager. It is lazily created.
     * @return the RootManager instance.
     */
    public static RootManager getInstance(){
        return RootManager.SingletonHelper.INSTANCE;
    }

    // Jackson has trouble with non static inner classes
    // http://cowtowncoder.com/blog/archives/2010/08/entry_411.html
    @JsonPropertyOrder(alphabetic=true)
    static private class InternalSettings {

        @JsonProperty("Image")
        @JsonFilter("Only Settings")
        private final ImageManager imageManager;

        @JsonProperty("Camera")
        @JsonFilter("Only Settings")
        private final CameraManager cameraManager;

        @JsonProperty("Picture Storage")
        @JsonFilter("Only Settings")
        private final StorageManager pictureStorageManager;

        @JsonProperty("Log Storage")
        @JsonFilter("Only Settings")
        private final StorageManager logStorageManager;

        @JsonProperty("Take Picture Triggers")
        @JsonFilter("Only Settings")
        private final TriggersManager takePictureTriggersManager;

        //@JsonProperty("General")
        //@JsonFilter("Only Settings")
        //private final SettingsManager setManager;

        // FIXME it may merge with the above
        @JsonProperty("Settings File Path")
        private final String settingsFilePath;

        InternalSettings() {
            settingsFilePath = "/sdcard/";
            imageManager = new ImageManager();
            cameraManager = new CameraManager();
            pictureStorageManager = new LocalStorageManager();
            logStorageManager = new LocalStorageManager();
            takePictureTriggersManager = new TriggersManager();
            //setManager = new SettingsManager();
        }

    }


    public String getSettingsFilePath() {
        return new String(internalSettings.settingsFilePath);
    }

    public ImageManager getImageManager(){
        return internalSettings.imageManager;
    }

    public CameraManager getCameraManager(){
        return internalSettings.cameraManager;
    }

    public StorageManager getPictureStorageManager(){
        return internalSettings.pictureStorageManager;
    }

    public StorageManager getLogStorageManager(){
        return internalSettings.logStorageManager;
    }

    public TriggersManager getTakePictureTriggersManager(){
        return internalSettings.takePictureTriggersManager;
    }

    //public SettingsManager getSettingsManager() { return internalSettings.setManager; }

    /**
     * Reads settings file into a string and instantiates the hierarchy of RootSettings objects.
     * If the read fails for any reason, a new RootSettings class is instantiated with its
     * hard coded, default values.
     *
     * @return reference to the internal settings
     */
    public RootManager readSettingsFile(){

        File file = new File(getSettingsFilePath(),"mbw2_config.txt");
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();

            return readSettingsJSON(text.toString());

        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "Configuration file not found at "+ getSettingsFilePath() +"!");
            Log.v(LOG_TAG, "Exact exception is :", e);

        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException while reading the config file!");
            Log.v(LOG_TAG, "Exact exception is :", e);
        }
        return null;
    }

    /**
     * Reads JSON string and instantiates the hierarchy of RootSettings objects.
     */
    private RootManager readSettingsJSON(String settings) {
        try {
            return objectMapper.setFilterProvider(serializationFilters)
                    .readValue(settings, RootManager.class);
        } catch (IOException e) {
            Log.e(LOG_TAG, "readSettingsJSON throw an IOException while reading the JSON string!", e);
        }
        return null;
    }

    /**
     * Serializes the internal settings of the application.
     * FIXME this one should not be needed!
     */
    private String writeConfigFile(Object manager) {
        try {
            return objectMapper.setFilterProvider(serializationFilters)
                    .writerWithDefaultPrettyPrinter().writeValueAsString(manager);
        } catch (JsonProcessingException e){
            Log.e(LOG_TAG, "JSON Processing exception!", e);
        }
        return null;

    }


    @Override
    public String toString(){
        String repr =  "\n******\nSettings Overview:\n";
        repr += "\n\tImage Settings:\n" + getImageManager();
        repr += "\n\tCamera Settings:\n" + getCameraManager();
        repr += "\n\tPictures Storage Settings:\n" + getPictureStorageManager();
        repr += "\n\tLogs Storage Settings:\n" + getLogStorageManager();
        repr += "\n\tPicture Triggers Settings:\n" + getTakePictureTriggersManager();
        //repr += "\n\tGeneral Settings:\n" + getSettingsManager();
        repr += "\n\tSettings File Path: " + getSettingsFilePath() + "\n";
        repr += "******\n";
        return repr;
    }


    // FIXME make a proper test out of this!!
    public void testJSONSerialization(){
        String sf = writeConfigFile(this);
        Log.d(LOG_TAG, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        Log.d(LOG_TAG, "" + sf );
        Log.d(LOG_TAG, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        Log.d(LOG_TAG, "" + readSettingsJSON(sf) );
        Log.d(LOG_TAG, "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    }

}
