package com.citysdk.demo.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.citysdk.demo.domain.CategoryDomain;
import com.citysdk.demo.invoker.CategoriesInvoker;
import com.citysdk.demo.invoker.Invoker;
import com.citysdk.demo.invoker.ListEventInvoker;
import com.citysdk.demo.invoker.ListPoiInvoker;
import com.citysdk.demo.invoker.ListRouteInvoker;
import com.citysdk.demo.invoker.PoiInvoker;
import com.citysdk.demo.listener.OnResultsListener;

import citysdk.tourism.client.exceptions.InvalidParameterException;
import citysdk.tourism.client.exceptions.InvalidParameterTermException;
import citysdk.tourism.client.exceptions.InvalidValueException;
import citysdk.tourism.client.exceptions.ResourceNotAllowedException;
import citysdk.tourism.client.exceptions.ServerErrorException;
import citysdk.tourism.client.exceptions.UnknownErrorException;
import citysdk.tourism.client.exceptions.VersionNotAvailableException;
import citysdk.tourism.client.parser.DataReader;
import citysdk.tourism.client.parser.data.ImageContent;
import citysdk.tourism.client.poi.lists.ListEvent;
import citysdk.tourism.client.poi.lists.POIS;
import citysdk.tourism.client.poi.single.Category;
import citysdk.tourism.client.poi.single.POI;
import citysdk.tourism.client.poi.single.PointOfInterest;
import citysdk.tourism.client.requests.Parameter;
import citysdk.tourism.client.requests.ParameterList;
import citysdk.tourism.client.requests.TourismClient;
import citysdk.tourism.client.requests.TourismClientFactory;
import citysdk.tourism.client.terms.ParameterTerms;
import citysdk.tourism.client.terms.ResourceTerms;
import citysdk.tourism.client.terms.Term;

final public class TourismAPI {

	private static final String PREFS_NAME = "PrefsEndpoint";
	private static final String URL_ENDPOINT = "urlEndpoint";
	private static final String URL_LOCALE = "urlEndpointLocale";

	static String uriDirectory = "http://directory.citysdk.cm-lisboa.pt/resources";

	static public void setURL(Context context, String url, String locale) {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(URL_ENDPOINT, url);
		editor.putString(URL_LOCALE, locale);
		editor.commit();
	}

	static public Object[] getURL(Context context) {

		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

		String[] arr = (settings.getString(URL_LOCALE, "")).split("-");
		Locale locale;
		if(arr != null && arr.length==2) {
			locale = new Locale(arr[0], arr[1]);
		} else {
			locale = new Locale("en", "GB");
		}


		Object[] array = {settings.getString(URL_ENDPOINT, ""), locale};
		return array;
	}

	static public  TourismClient getClientWithUrl(String url)
			throws IOException, UnknownErrorException, ServerErrorException {
		TourismClient client = null;

		TourismClientFactory factory = TourismClientFactory.getInstance();

		client = factory.getClient(url);

		return client;
	}

	static public ListEvent getListEvents(ParameterList parameterList, String url) 
			throws InvalidParameterException, IOException, ResourceNotAllowedException,
			UnknownErrorException, ServerErrorException, VersionNotAvailableException,
			InvalidValueException {

		ListEvent list = null;

		TourismClient client = getClientWithUrl(url);

		client.useVersion("1.0");

		list = client.getEvents(parameterList);

		return list;
	}


	static public List<CategoryDomain> getCategoriesInformation(Context context, POI poi, String parameterTerm) {
		List<CategoryDomain> categoryList = new ArrayList<CategoryDomain>();

		Locale locale = (Locale) getURL(context)[1];
		Category category = (Category) poi;

		getSubCategoriesInformation(category, categoryList, locale, parameterTerm);
		return categoryList;
	}

	static public void getSubCategoriesInformation(Category category, List<CategoryDomain> categoryList, Locale locale, String parameterTerm) {
		List<Category> categories = category.getSubCategories();
		if (categories != null) {
			for (Category cat : categories) {


				String name = DataReader.getLabel(cat, Term.LABEL_TERM_PRIMARY, locale);
				String id = cat.getId();
				String option = parameterTerm;

				if (name != null && id != null && option != null) {
					categoryList.add(new CategoryDomain(id, option, name));
				}

				if (cat.getNumCategories() > 0) {
					getSubCategoriesInformation(cat, categoryList, locale, parameterTerm);
				}
			}
		}
	}



	static public void getCategories(final ParameterList list, final String url)
			throws IOException, UnknownErrorException, InvalidParameterTermException,
			ServerErrorException, ResourceNotAllowedException,
			VersionNotAvailableException, InvalidParameterException,
			InvalidValueException {

		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				try {

					Category categories = null;

					TourismClient client = getClientWithUrl(url);

					client.useVersion("1.0");

					categories = client.getCategories(list);


				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		thread.start(); 
	}

	static private PointOfInterest getPoiWithId(String uri, String base, String id)
			throws InvalidParameterException, IOException, ResourceNotAllowedException,
			UnknownErrorException, ServerErrorException, VersionNotAvailableException,
			InvalidValueException {



		PointOfInterest poi = null;

		TourismClient client = getClientWithUrl(uri);

		client.useVersion("1.0");

		poi = client.getPoi(base, id);

		return poi;
	}

	static public void parseItems(POIS<POI> poi) {
		String label = null;
		String description = null;
		String thumbnail = null;
		String image = null;

		// define the default language to be used, in case the wanted language
		// does not exist. The default language - if this method is not called - is en_GB.
		DataReader.setDefaultLocale(new Locale("en","GB"));

		// get the default locale
		Locale locale = new Locale("pt", "PT");

		for(int i = 0; i < poi.size(); i++) {
			// go through the list of objects
			POI item = poi.get(i);

			// get the primary label in PT language
			label = DataReader.getLabel(item, Term.LABEL_TERM_PRIMARY, locale);

			// get a description in PT language
			description = DataReader.getDescription(item, locale);

			// get a thumbnail (URI or base-64)
			List<ImageContent> img = DataReader.getThumbnails(item);
			ImageContent imgContent = null;
			if(img.size() > 0) {
				imgContent = img.get(0);
				thumbnail = imgContent.getContent();
			}

			// get an image (always a URI)
			List<ImageContent> imgUri = DataReader.getImagesUri(item);
			if(imgUri.size() > 0)
				image = imgUri.get(0).getContent();

			// print the values
			System.out.println("LABEL: " + label);
			System.out.println("DESCRIPTION: " + description);
			if(imgContent != null) {
				System.out.println("THUMBNAIL (URI?: " + imgContent.hasImgUri() + ")" + 
						";(BYTE-CODE?: " + imgContent.hasImgByteCode() + ") : " + thumbnail);
			} else {
				System.out.println("THUMBNAIL: " + thumbnail);
			}

			System.out.println("IMAGE: " + image);
		}
	}

	//	private void searchPois(boolean showMap) throws InvalidParameterException,
	//	InvalidValueException {
	//		ParameterList list = new ParameterList();
	//
	//		//		getDescriptionText(list, ParameterTerms.MINIMAL);
	//		//		getCategoryItems(list);
	//		//		getTagItems(list);
	//		getSearchLocation(list);
	//
	//		checkShowResults(list, ResourceTerms.FIND_POI);
	//		//		getData(MobileGuideActivity.PLACES,
	//		//				new ListPoiInvoker().setVersion(MobileGuideActivity.VERSION),
	//		//				getListener(showMap), list);
	//	}

	//	private String getSearchLocation(LatLng location, int radius) {
	//		String value = "";
	//		value = location.latitude + " " + location.longitude + " " + radius;
	//
	//		return value;
	//	}
	//
	//	private void getSearchLocation(ParameterList list)
	//			throws InvalidParameterException, InvalidValueException {
	//		String value;
	//		if (!((value = getSearchLocation(new LatLng(38.71431, -9.14114), 1)).equals("")))
	//			list.add(new Parameter(ParameterTerms.COORDS, value));
	//	}
	//
	//	private void getDescriptionText(ParameterList list, ParameterTerms term, String descriptionText)
	//			throws InvalidParameterException, InvalidValueException {
	//		if (!descriptionText.equals(""))
	//			list.add(new Parameter(term, descriptionText));
	//	}

	private static void checkShowResults(String uri, ParameterList list, ResourceTerms resource) {
		//		SharedPreferences prefs = PreferenceManager
		//				.getDefaultSharedPreferences();
		//		boolean enabled = prefs.getBoolean("parent_results_preference", false);


		try {
			TourismClient client = getClientWithUrl(uri);

			if (client.hasResourceParameter(resource, ParameterTerms.LIMIT)) {
				String results = ""+5;
				list.add(new Parameter(ParameterTerms.LIMIT, Integer
						.parseInt(results)));
				list.add(new Parameter(ParameterTerms.OFFSET, 0));
			} else {
				list.add(new Parameter(ParameterTerms.LIMIT, -1));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnknownErrorException e) {
			e.printStackTrace();
		} catch (ServerErrorException e) {
			e.printStackTrace();
		} catch (VersionNotAvailableException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (InvalidParameterException e) {
			e.printStackTrace();
		} catch (InvalidValueException e) {
			e.printStackTrace();
		}
	}

	static public void getEndpoint(OnResultsListener resultListener,  ParameterList list) {
		ListPoiInvoker invoker = new ListPoiInvoker();
		invoker.setVersion("1.0");
		invoker.setItemId(2);	
		getDataCategories(uriDirectory, 0, invoker, resultListener, list, "");
	}

	static public void getSinglePoi(Context context, OnResultsListener resultListener,  ParameterList list) {
		PoiInvoker invoker = new PoiInvoker();
		String id = (String) list.getWithTerm(ParameterTerms.ID).getValue();
		String base = (String)list.getWithTerm(ParameterTerms.BASE).getValue();
		invoker.setBase(base);
		invoker.setId(id);
		invoker.setVersion("1.0");
		invoker.setItemId(0);		 
		getData(context, 0, invoker, resultListener, list, "");
	}

	static public void getCategories(Context context, OnResultsListener resultListener,  ParameterList list) {
		String uri = (String) getURL(context)[0];

		CategoriesInvoker invoker = new CategoriesInvoker();
		invoker.setVersion("1.0");
		invoker.setItemId(0);
		invoker.setTerm((String)list.get(0).getValue());
		getDataCategories(uri, 0, invoker, resultListener, list, "");
	}

	static public void getPlaceCategories(Context context, OnResultsListener resultListener,  ParameterList list, String bytesOfMessage, String option) {
		ListPoiInvoker invoker = new ListPoiInvoker();
		invoker.setVersion("1.0");
		invoker.setItemId(1);	
		invoker.setTerm(option);
		getData(context, 0, invoker, resultListener, list, bytesOfMessage);
	}

	static public void getEventCategories(Context context, OnResultsListener resultListener,  ParameterList list, String bytesOfMessage, String option) {
		ListEventInvoker invoker = new ListEventInvoker();
		invoker.setVersion("1.0");
		invoker.setItemId(1);
		invoker.setTerm(option);
		getData(context, 0, invoker, resultListener, list, bytesOfMessage);
	}

	static public void getItinerariesCategories(Context context, OnResultsListener resultListener,  ParameterList list, String bytesOfMessage, String option) {
		ListRouteInvoker invoker = new ListRouteInvoker();
		invoker.setVersion("1.0");
		invoker.setItemId(1);	
		invoker.setTerm(option);
		getData(context, 0, invoker, resultListener, list, bytesOfMessage);
	}

	static private void getDataCategories(String url, int search, Invoker invoker,
			OnResultsListener listener, ParameterList list, String bytesOfMessage) {			

		FetchTourismDataTask categoriestask = new FetchTourismDataTask(url, invoker, listener, bytesOfMessage);
		categoriestask.execute(list);

	}

	static private void getData(Context context, int search, Invoker invoker,
			OnResultsListener listener, ParameterList list, String bytesOfMessage) {			

		String uri = (String) getURL(context)[0];
		if (task != null && (task.getStatus().equals(AsyncTask.Status.PENDING) || task.getStatus().equals(AsyncTask.Status.RUNNING))) {
			System.out.println("task cancelled");
			task.cancel(true);
			task = new FetchTourismDataTask(uri, invoker, listener, bytesOfMessage);
			task.execute(list);
		} else {
			System.out.println("task executing");
			task = new FetchTourismDataTask(uri, invoker, listener, bytesOfMessage);
			task.execute(list);
		}		
	}

	static FetchTourismDataTask task;

}