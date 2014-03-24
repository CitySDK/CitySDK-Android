package org.citysdk.citysdkdemo.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import citysdk.tourism.client.exceptions.InvalidParameterException;
import citysdk.tourism.client.exceptions.InvalidValueException;
import citysdk.tourism.client.poi.lists.POIS;
import citysdk.tourism.client.poi.single.POI;
import citysdk.tourism.client.requests.Parameter;
import citysdk.tourism.client.requests.ParameterList;
import citysdk.tourism.client.terms.ParameterTerms;
import org.citysdk.citysdkdemo.activities.SettingsActivity;
import org.citysdk.citysdkdemo.contracts.PoisContract;
import org.citysdk.citysdkdemo.contracts.PoisContract.Pois;
import org.citysdk.citysdkdemo.domain.CategoryDomain;
import org.citysdk.citysdkdemo.listener.OnResultsListener;
import org.citysdk.citysdkdemo.utils.FileManager;
import org.citysdk.citysdkdemo.utils.TourismAPI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


class SyncAdapter extends AbstractThreadedSyncAdapter implements OnResultsListener {

	public static String TAG = "SyncAdapter";

	public static final String PREFS_NAME = "MyPrefsFile";

	public static final String SEPARATOR = "Foo-";

	private final ContentResolver mContentResolver;

	private Context context;
	
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		mContentResolver = context.getContentResolver();
		this.context = context;
	}

	public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
		mContentResolver = context.getContentResolver();
		this.context = context;
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {

		String type = extras.getString("type");
		Log.i(TAG, "Beginning network synchronization: "+ type);


		if(type == null || type.equalsIgnoreCase("full") || type.equalsIgnoreCase("categories")) {
			getCategories(ParameterTerms.POIS.getTerm());
			getCategories(ParameterTerms.EVENTS.getTerm());
			getCategories(ParameterTerms.ROUTES.getTerm());
			
		}
//		if(type != null && type.equalsIgnoreCase("search")) {			
//			try {
//
//				getPois();
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			} catch (InvalidParameterException e) {
//				e.printStackTrace();
//			} catch (InvalidValueException e) {
//				e.printStackTrace();
//			} catch (OperationApplicationException e) {
//				e.printStackTrace();
//			}
//		}
	}

//	private Context getContext() {
//		if (context == null) {
//			getContext();
//		} else {
//			return context;
//		}
//	}
//	private void checkFiles() throws RemoteException, OperationApplicationException {
//
//		final ContentResolver contentResolver = getContext().getContentResolver();
//
//		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
//
//		List<String> filesToRemove = new ArrayList<String>(FileManager.listFiles(getContext().getApplicationContext()));
//
//		Cursor cursor = contentResolver.query(
//				PoisContract.Pois.CONTENT_URI,
//				PoisContract.Pois.PROJECTION_POIS,
//				null,
//				null,
//				null);
//
//		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
//			String filename = cursor.getString(Pois.POIS_COLUMN_QUERY);
//			if(FileManager.listFiles(getContext().getApplicationContext()).contains(filename)) {
//				filesToRemove.remove(filename);
//			} else {
//
//				Uri deleteUri = PoisContract.Pois.CONTENT_URI.buildUpon()
//						.appendPath(Integer.toString(cursor.getInt(Pois.POIS_COLUMN_ID)))
//						.build();
//
//				batch.add(ContentProviderOperation.newDelete(deleteUri).build());
//			}
//		}
//
//		for(String s : filesToRemove) {
//			FileManager.removeFiles(getContext().getApplicationContext(), s);
//		}
//		mContentResolver.applyBatch(PoisContract.CONTENT_AUTHORITY, batch);
//		mContentResolver.notifyChange(
//				PoisContract.Pois.CONTENT_URI,
//				null,
//				false);
//
//		cursor.close();
//
//	}

	private void getCategories(String pois) {
		try {
			ParameterList list = new ParameterList();
			list.add(new Parameter(ParameterTerms.LIST, pois));
			list.add(new Parameter(ParameterTerms.LIMIT, -1));

			TourismAPI.getCategories(getContext(), this, list);

		} catch (InvalidParameterException e) {
			e.printStackTrace();
		} catch (InvalidValueException e) {
			e.printStackTrace();
		}

	}

//	private void getPois() throws InvalidParameterException, InvalidValueException, RemoteException, OperationApplicationException {
//
//		SharedPreferences userDetails = getContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);		
//		String selectedOptions = userDetails.getString("selectedOptions", "");
//		List<String> selectedCategories = new ArrayList<String>(userDetails.getStringSet("selectedCategories", new HashSet<String>()));
//
//		//NO CATEGORIES - ADD EMPTY ROW
//		if(selectedCategories == null || selectedCategories.size() == 0) {
//			try {
//				if(hasPoi("", "")) {
//					updateAccessPoi("", "");
//				} else {
//					addPoiToSQL("", "");
//				}
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			} catch (OperationApplicationException e) {
//				e.printStackTrace();
//			}
//			return;
//		}
//
//		java.util.Collections.sort(selectedCategories);
//
//		String bytesOfMessage = "";
//
//		ParameterList list = new ParameterList();
//
//		//CALCULATE QUERY HASH
//		//if(!selectedCategories.contains("ALL!")) {
//		if(selectedCategories.contains("ALL!")) {
//			selectedCategories.remove("ALL!");
//		}
//		list.add(new Parameter(ParameterTerms.CATEGORY, selectedCategories));
//		bytesOfMessage = SEPARATOR+selectedOptions.hashCode()+selectedCategories.hashCode();
//		//} else {
//		//bytesOfMessage = SEPARATOR+selectedOptions.hashCode()+"ALL!".hashCode();
//		//}
//		list.add(new Parameter(ParameterTerms.LIMIT, -1));
//
//		if(hasPoi(selectedOptions, bytesOfMessage)) {
//			updateAccessPoi(selectedOptions, bytesOfMessage);
//			return;
//		}
//
//		if(selectedOptions.equalsIgnoreCase("places")) {
//			TourismAPI.getPlaceCategories(getContext(), this, list, bytesOfMessage, selectedOptions);
//		} else if(selectedOptions.equalsIgnoreCase("events")) {
//
//			list.add(new Parameter(ParameterTerms.TIME, getTimeParam()));
//
//			TourismAPI.getEventCategories(getContext(), this, list, bytesOfMessage, selectedOptions);
//		} else if(selectedOptions.equalsIgnoreCase("itineraries")) {
//			TourismAPI.getItinerariesCategories(getContext(), this, list, bytesOfMessage, selectedOptions);
//		}
//	}	


	@Override
	public void onResultsFinished(POI poi, int id, String parameterTerm, String bytesOfMessage) {
		if(id == 0) {
			if (poi == null) {
				return;
			}

			List<CategoryDomain> list = new ArrayList<CategoryDomain>();
			list = TourismAPI.getCategoriesInformation(context, poi, parameterTerm.toString());
			try {
				handleCategories(list, parameterTerm);

			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}
		} 		
	}



	private String getTimeParam() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
		String syncConnPref = sharedPref.getString(SettingsActivity.PREF_MENU_EVENTS_DAYS, "7");

		Calendar c = Calendar.getInstance();	    

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateandTime = sdf.format(c.getTime());

		c.add(Calendar.DAY_OF_YEAR, Integer.parseInt(syncConnPref));
		String formattedDate = sdf.format(c.getTime());

		return currentDateandTime+ " "+ formattedDate;
	}

//	private void updateAccessPoi(String parameterTerm, String bytesOfMessage) throws RemoteException, OperationApplicationException {
//
//		final ContentResolver contentResolver = getContext().getContentResolver();
//
//		//IF IT IS ALREADY THE FIRST ROW
//
//		Cursor cursorAux = contentResolver.query(
//				PoisContract.Pois.CONTENT_URI,
//				PoisContract.Pois.PROJECTION_POIS,
//				null,
//				null,
//				PoisContract.Pois.COLUMN_POIS_DATE_ACCESSED+ " DESC LIMIT 1");
//		cursorAux.moveToFirst();
//		if(cursorAux.getCount()>0 && cursorAux.getString(PoisContract.Pois.POIS_COLUMN_QUERY).equalsIgnoreCase(bytesOfMessage+"")) {
//			Log.i(TAG, "Already has POIs information");
//			cursorAux.close();
//			return;
//		}
//		cursorAux.close();
//
//
//		//IF IT IS NOT THE FIRST ROW - CHANGE ACCESS DATE
//		Cursor c = contentResolver.query(
//				PoisContract.Pois.CONTENT_URI, 
//				PoisContract.Pois.PROJECTION_POIS,
//				PoisContract.Pois.COLUMN_POIS_TYPE + "= '" +  parameterTerm + "' AND " + PoisContract.Pois.COLUMN_POIS_QUERY + " = '" + bytesOfMessage +"'",
//				null, 
//				null);
//
//		if (c != null && c.getCount() > 0) {
//
//			c.moveToFirst();
//
//			long unixTime = System.currentTimeMillis() / 100L;
//
//			Uri existingUri = PoisContract.Pois.CONTENT_URI.buildUpon().appendPath(Integer.toString(c.getInt(PoisContract.Pois.POIS_COLUMN_ID))).build();
//
//			ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
//
//			batch.add(ContentProviderOperation.newUpdate(existingUri)
//					.withValue(PoisContract.Pois.COLUMN_POIS_DATE_ACCESSED, unixTime)
//					.build());
//
//			mContentResolver.applyBatch(PoisContract.CONTENT_AUTHORITY, batch);
//			mContentResolver.notifyChange(
//					PoisContract.Pois.CONTENT_URI,
//					null,
//					false);
//			c.close();
//		}
//		c.close();
//
//	}

//	private boolean hasPoi(String parameterTerm, String bytesOfMessage) {
//
//		final ContentResolver contentResolver = getContext().getContentResolver();
//
//		Uri uri = PoisContract.Pois.CONTENT_URI;
//		Cursor c = contentResolver.query(uri, PoisContract.Pois.PROJECTION_POIS, PoisContract.Pois.COLUMN_POIS_TYPE + "= '" +  parameterTerm + 
//				"' AND " + PoisContract.Pois.COLUMN_POIS_QUERY + " = '" + bytesOfMessage +"'", null, null);
//
//		if (c != null && c.getCount() > 0) {
//			c.close();
//			Log.i(TAG, "Already has POIs information");
//			return true;
//		}
//		c.close();
//
//		Log.i(TAG, "Does not have POIs information");
//		return false;
//	}



//	private void hanglePois(POIS<POI> poi, String parameterTerm, String bytesOfMessage) throws RemoteException, OperationApplicationException {
//
//		if(poi == null && parameterTerm == null && bytesOfMessage.equalsIgnoreCase("")) {
//			addPoiToSQL("", "");
//			return;
//		}
//		FileManager.writePoisToFile(getContext().getApplicationContext(), bytesOfMessage, poi);
//		addPoiToSQL( parameterTerm, bytesOfMessage);
//	}
//
//	private void addPoiToSQL(String parameterTerm, String bytesOfMessage) throws RemoteException, OperationApplicationException {
//		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
//
//		long unixTime = System.currentTimeMillis() / 100L;
//
//		batch.add(ContentProviderOperation.newInsert(PoisContract.Pois.CONTENT_URI)
//				.withValue(PoisContract.Pois.COLUMN_POIS_TYPE, parameterTerm)
//				.withValue(PoisContract.Pois.COLUMN_POIS_QUERY, bytesOfMessage)
//				.withValue(PoisContract.Pois.COLUMN_POIS_DATE_CREATED, unixTime)
//				.withValue(PoisContract.Pois.COLUMN_POIS_DATE_ACCESSED, unixTime)
//				.build());
//
//		mContentResolver.applyBatch(PoisContract.CONTENT_AUTHORITY, batch);
//		mContentResolver.notifyChange(
//				PoisContract.Pois.CONTENT_URI,
//				null,
//				false);	
//	}


	private void handleCategories(List<CategoryDomain> list, String parameterTerm) throws RemoteException, OperationApplicationException {

		SyncResult syncResult = new SyncResult();

		final ContentResolver contentResolver = getContext().getContentResolver();

		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

		HashMap<String, CategoryDomain> entryMap = new HashMap<String, CategoryDomain>();
		if (list != null) {
			for (CategoryDomain e : list) {
				e.setOption(parameterTerm);
				entryMap.put(e.getId(), e);
			}
		}
		Uri uri = PoisContract.Category.CONTENT_URI;
		Cursor c = contentResolver.query(uri, PoisContract.Category.PROJECTION_CATEGORY, PoisContract.Category.COLUMN_CATEGORY_OPTION + "= '" +  parameterTerm + "'", null, null);
		assert c != null;

		int id;
		String oid;
		String option;
		String name;

		while (c.moveToNext()) {
			syncResult.stats.numEntries++;
			id = c.getInt(PoisContract.Category.CATEGORY_COLUMN_ID);
			oid = c.getString(PoisContract.Category.CATEGORY_COLUMN_OID);
			option = c.getString(PoisContract.Category.CATEGORY_COLUMN_OPTION);
			name = c.getString(PoisContract.Category.CATEGORY_COLUMN_NAME);

			CategoryDomain match = entryMap.get(oid);
			if (match != null) {
				entryMap.remove(oid);
				Uri existingUri = PoisContract.Category.CONTENT_URI.buildUpon().appendPath(Integer.toString(id)).build();

				if ((match.getId() != null && !match.getId().equals(oid)) ||
						(match.getOption() != null && !match.getOption().equals(option)) ||
						(match.getName() != null && !match.getName().equals(name))) {

					batch.add(ContentProviderOperation.newUpdate(existingUri)
							.withValue(PoisContract.Category.COLUMN_CATEGORY_OID, match.getId())
							.withValue(PoisContract.Category.COLUMN_CATEGORY_OPTION, match.getOption())
							.withValue(PoisContract.Category.COLUMN_CATEGORY_NAME, match.getName())
							.build());
					syncResult.stats.numUpdates++;
				} else {
				}
			} else {
				Uri deleteUri = PoisContract.Category.CONTENT_URI.buildUpon()
						.appendPath(Integer.toString(id)).build();
				batch.add(ContentProviderOperation.newDelete(deleteUri).build());
				syncResult.stats.numDeletes++;
			}
		}
		c.close();

		if (entryMap != null) {
			for (CategoryDomain e : entryMap.values()) {
				batch.add(ContentProviderOperation.newInsert(PoisContract.Category.CONTENT_URI)
						.withValue(PoisContract.Category.COLUMN_CATEGORY_OID, e.getId())
						.withValue(PoisContract.Category.COLUMN_CATEGORY_OPTION, e.getOption())
						.withValue(PoisContract.Category.COLUMN_CATEGORY_NAME, e.getName())
						.build());
				syncResult.stats.numInserts++;
			}
		}
		mContentResolver.applyBatch(PoisContract.CONTENT_AUTHORITY, batch);
		mContentResolver.notifyChange(
				PoisContract.Category.CONTENT_URI,
				null,
				false);	
	}
}