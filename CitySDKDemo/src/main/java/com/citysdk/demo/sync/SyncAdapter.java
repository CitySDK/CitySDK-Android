package com.citysdk.demo.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.citysdk.demo.contracts.PoisContract;
import com.citysdk.demo.domain.CategoryDomain;
import com.citysdk.demo.listener.OnResultsListener;
import com.citysdk.demo.utils.TourismAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import citysdk.tourism.client.exceptions.InvalidParameterException;
import citysdk.tourism.client.exceptions.InvalidValueException;
import citysdk.tourism.client.poi.single.POI;
import citysdk.tourism.client.requests.Parameter;
import citysdk.tourism.client.requests.ParameterList;
import citysdk.tourism.client.terms.ParameterTerms;


class SyncAdapter extends AbstractThreadedSyncAdapter implements OnResultsListener {

    private final ContentResolver mContentResolver;
    private final static String TAG = "SyncAdapter";
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
        Log.i(TAG, "onPerformSync()");

        getCategories(ParameterTerms.POIS.getTerm());
        getCategories(ParameterTerms.EVENTS.getTerm());
        getCategories(ParameterTerms.ROUTES.getTerm());
    }

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

    @Override
    public void onResultsFinished(POI poi, int id, String parameterTerm, String bytesOfMessage) {
        if (id == 0) {
            if (poi == null) {
                return;
            }

            List<CategoryDomain> list = TourismAPI.getCategoriesInformation(context, poi,
                    parameterTerm.toString());

            try {
                handleCategories(list, parameterTerm);

            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }
        }

    }

    private void handleCategories(List<CategoryDomain> list, String parameterTerm) throws
            RemoteException, OperationApplicationException {

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
        Cursor c = contentResolver.query(uri, PoisContract.Category.PROJECTION_CATEGORY,
                PoisContract.Category.COLUMN_CATEGORY_OPTION + "= '" + parameterTerm + "'", null,
                null);
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
                Uri existingUri = PoisContract.Category.CONTENT_URI.buildUpon().appendPath
                        (Integer.toString(id)).build();

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
//        mContentResolver.notifyChange(
//                PoisContract.Category.CONTENT_URI,
//                null,
//                false);
    }
}