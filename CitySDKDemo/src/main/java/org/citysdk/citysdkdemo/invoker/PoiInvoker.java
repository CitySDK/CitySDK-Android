package org.citysdk.citysdkdemo.invoker;

import java.io.IOException;

import citysdk.tourism.client.exceptions.ServerErrorException;
import citysdk.tourism.client.exceptions.UnknownErrorException;
import citysdk.tourism.client.requests.TourismClient;
import citysdk.tourism.client.requests.TourismClientFactory;
import citysdk.tourism.client.requests.ParameterList;
import citysdk.tourism.client.terms.ParameterTerms;
import citysdk.tourism.client.poi.single.POI;

public class PoiInvoker extends Invoker {
	
	@Override
	public POI invoke(ParameterList parameterList, String homeUrl) {
		try {
			TourismClient client = TourismClientFactory.getInstance().getClient(homeUrl);
			client.useVersion(version);
			return client.getPoi(base, id);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnknownErrorException e) {
			e.printStackTrace();
		} catch (ServerErrorException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
