package com.citysdk.demo.utils;


import android.os.AsyncTask;

import com.citysdk.demo.invoker.Invoker;
import com.citysdk.demo.listener.OnResultsListener;

import citysdk.tourism.client.poi.single.POI;
import citysdk.tourism.client.requests.ParameterList;

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
    protected POI doInBackground(ParameterList... params) {
        return invoker.invoke(params[0], homeUrl);
    }

    @Override
    protected void onPostExecute(POI result) {
        listener.onResultsFinished(result, invoker.getItemId(), invoker.getTerm(), bytesOfMessage);
    }
}
