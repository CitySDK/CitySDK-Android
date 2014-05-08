package com.citysdk.demo.invoker;

import java.io.IOException;

import citysdk.tourism.client.exceptions.InvalidParameterException;
import citysdk.tourism.client.exceptions.ResourceNotAllowedException;
import citysdk.tourism.client.exceptions.ServerErrorException;
import citysdk.tourism.client.exceptions.UnknownErrorException;
import citysdk.tourism.client.exceptions.VersionNotAvailableException;
import citysdk.tourism.client.poi.lists.ListEvent;
import citysdk.tourism.client.poi.single.POI;
import citysdk.tourism.client.requests.ParameterList;
import citysdk.tourism.client.requests.TourismClient;
import citysdk.tourism.client.requests.TourismClientFactory;

public class ListEventInvoker extends Invoker {

    private TourismClient client;

    @Override
    public POI invoke(ParameterList parameterList, String homeUrl) {
        try {
            client = TourismClientFactory.getInstance().getClient(homeUrl);
            if (client != null) {
                client.useVersion(version);
                ListEvent list = client.getEvents(parameterList);
                return list;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        } catch (UnknownErrorException e) {
            e.printStackTrace();
        } catch (ServerErrorException e) {
            e.printStackTrace();
        } catch (ResourceNotAllowedException e) {
            e.printStackTrace();
        } catch (VersionNotAvailableException e) {
            e.printStackTrace();
        }

        return null;
    }
}
