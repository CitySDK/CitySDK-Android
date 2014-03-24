package com.citysdk.demo.utils;


import com.citysdk.demo.invoker.Invoker;
import com.citysdk.demo.listener.OnResultsListener;

import android.os.AsyncTask;
import citysdk.tourism.client.requests.ParameterList;
import citysdk.tourism.client.poi.single.POI;

public class FetchTourismDataTask extends AsyncTask<ParameterList, Void, POI> {
	private OnResultsListener listener;
	private String homeUrl;
	private Invoker invoker;
	private String bytesOfMessage;

	public FetchTourismDataTask(String homeUrl, Invoker invoker, OnResultsListener listener, String bytesOfMessage) {
		this.homeUrl = homeUrl;
		this.invoker = invoker;
		this.listener = listener;
		this.bytesOfMessage = bytesOfMessage;
	}

	@Override
	protected POI doInBackground(ParameterList ... params) {		
		return invoker.invoke(params[0], homeUrl);
	}
	
	@Override
	protected void onPostExecute(POI result) {
		listener.onResultsFinished(result, invoker.getItemId(), invoker.getTerm(), bytesOfMessage);
	}
}
