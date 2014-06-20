package com.citysdk.demo.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AssetsPropertyReader {

    private Context context;
    private Properties properties;

    public AssetsPropertyReader(Context context) {
        this.context = context;
        properties = new Properties();
    }

    public Properties getProperties() {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("open311.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            Log.e("AssetsPropertyReader", e.toString());
        }
        return properties;
    }
}