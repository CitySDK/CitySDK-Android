package com.citysdk.demo.listener;

import citysdk.tourism.client.poi.single.POI;

public interface OnResultsListener {
    void onResultsFinished(POI poi, int id, String parameterTerm, String bytesOfMessage);
}
