package org.citysdk.citysdkdemo.listener;

import citysdk.tourism.client.poi.single.POI;
import citysdk.tourism.client.requests.ParameterList;
import citysdk.tourism.client.terms.ParameterTerms;

public interface OnResultsListener {
	void onResultsFinished(POI poi, int id, String parameterTerm, String bytesOfMessage);
}
