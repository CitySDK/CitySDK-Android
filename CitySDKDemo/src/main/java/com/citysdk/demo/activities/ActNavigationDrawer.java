package com.citysdk.demo.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.citysdk.demo.R;
import com.citysdk.demo.contracts.PoisContract;
import com.citysdk.demo.domain.CategoryDomain;
import com.citysdk.demo.listener.OnResultsListener;
import com.citysdk.demo.navigationdrawer.AbstractNavDrawerActivity;
import com.citysdk.demo.navigationdrawer.NavDrawerActivityConfiguration;
import com.citysdk.demo.navigationdrawer.NavDrawerAdapter;
import com.citysdk.demo.navigationdrawer.NavDrawerItem;
import com.citysdk.demo.navigationdrawer.NavMenuItem;
import com.citysdk.demo.navigationdrawer.NavMenuSection;
import com.citysdk.demo.sync.SyncUtils;
import com.citysdk.demo.utils.TourismAPI;
import com.citysdk.demo.utils.XmlParser;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;

import citysdk.tourism.client.exceptions.InvalidParameterException;
import citysdk.tourism.client.exceptions.InvalidValueException;
import citysdk.tourism.client.poi.base.POITermType;
import citysdk.tourism.client.poi.lists.POIS;
import citysdk.tourism.client.poi.single.POI;
import citysdk.tourism.client.requests.Parameter;
import citysdk.tourism.client.requests.ParameterList;
import citysdk.tourism.client.terms.ParameterTerms;
import citysdk.tourism.client.terms.Term;

public class ActNavigationDrawer extends AbstractNavDrawerActivity implements OnResultsListener, LocationListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "NavigationDrawer";
    private static final int FETCH_CATEGORIES = 0;
    private static String STR_PLACE = "places";
    private static String STR_EVENT = "events";
    private static String STR_ITINERARIES = "itineraries";
    private static String DB_PLACE = "poi";
    private static String DB_EVENT = "event";
    private static String DB_ITINERARIES = "route";
    private static String VIEW_MAPS = "maps";
    private static String VIEW_LIST = "list";

    private NavDrawerItem[] view, options, categories;

    private NavDrawerActivityConfiguration navDrawerActivityConfiguration;

    private POIS<POI> mPoi;
    private List<CategoryDomain> mCategories;

    private String selectedView = "";
    private String selectedOptions = "";

    private ArrayList<String> selectedCategories = new ArrayList<String>();
    //private ArrayList<String> selectedCategoriesLastSearch = new ArrayList<String>();


    private ArrayList<String> selectedPlaces = new ArrayList<String>();
    private ArrayList<String> selectedEvents = new ArrayList<String>();
    private ArrayList<String> selectedItineraries = new ArrayList<String>();

    private MapsActivity mapsActivity;
    private ListActivity listActivity;

    private LocationManager locationManager;
    private String provider;

    private ObservableClass observerClass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        //criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        observerClass = new ObservableClass();
        locationManager.requestLocationUpdates(provider, 1000, 1, this);

        if (location != null) {
            onLocationChanged(location);
        }
        if (mapsActivity == null) {
            mapsActivity = new MapsActivity(this);
        }

        if (listActivity == null) {
            listActivity = new ListActivity(this);
        }

        mCategories = new ArrayList<CategoryDomain>();

        view = generateView("View", 100, "menu_view.xml");

        options = generateView("Options", 200, "menu_options.xml");

        SharedPreferences userDetails = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        selectedView = userDetails.getString("selectedView", "");
        if (selectedView.equalsIgnoreCase("")) {
            selectedView = view[1].getLabel();
        }
        selectedOptions = userDetails.getString("selectedOptions", "");
        if (selectedOptions.equalsIgnoreCase("")) {
            selectedOptions = options[1].getLabel();
            saveMenuOptions();
        }

        SyncUtils.CreateSyncAccount(getApplicationContext());

        SyncUtils.TriggerRefresh();


        getSupportLoaderManager().initLoader(FETCH_CATEGORIES, null, this);


        FragmentManager supportFragmentManager = getSupportFragmentManager();
        supportFragmentManager.beginTransaction().add(R.id.content_frame,
                mapsActivity).commit();
        supportFragmentManager.beginTransaction().add(R.id.content_frame,
                listActivity).commit();

        if (selectedView.equalsIgnoreCase(VIEW_MAPS)) {
            supportFragmentManager.beginTransaction().detach(listActivity).commit();
            supportFragmentManager.beginTransaction().attach(mapsActivity).commit();
        } else if (selectedView.equalsIgnoreCase(VIEW_LIST)) {
            supportFragmentManager.beginTransaction().detach(mapsActivity).commit();
            supportFragmentManager.beginTransaction().attach(listActivity).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 1000, 1, this);

        SharedPreferences userDetails = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        selectedCategories = new ArrayList<String>(userDetails.getStringSet("selectedCategories", new HashSet<String>()));

        selectedPlaces = new ArrayList<String>(userDetails.getStringSet("selectedPlaces", new HashSet<String>()));
        selectedEvents = new ArrayList<String>(userDetails.getStringSet("selectedEvents", new HashSet<String>()));
        selectedItineraries = new ArrayList<String>(userDetails.getStringSet("selectedItineraries", new HashSet<String>()));

        selectedCategories = new ArrayList<String>(userDetails.getStringSet("selectedCategories", new HashSet<String>()));

        updateSetSelected(selectedView, 100);
        updateSetSelected(selectedOptions, 200);
        updateSetSelectedCategories(selectedOptions, 300);

        //changeOptions();
        if (selectedOptions.equalsIgnoreCase(STR_PLACE)) {
            selectedCategories = new ArrayList<String>(selectedPlaces);
        } else if (selectedOptions.equalsIgnoreCase(STR_EVENT)) {
            selectedCategories = new ArrayList<String>(selectedEvents);
        } else if (selectedOptions.equalsIgnoreCase(STR_ITINERARIES)) {
            selectedCategories = new ArrayList<String>(selectedItineraries);
        }
        //performSearch();
        if (mPoi == null || mPoi.size() == 0) {
            getPois();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        saveMenuOptions();
    }

    private void saveMenuOptions() {
        SharedPreferences preferences = getSharedPreferences("sharedPrefs", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("selectedView", selectedView);
        editor.putString("selectedOptions", selectedOptions);
        editor.putStringSet("selectedPlaces", new HashSet<String>(selectedPlaces));
        editor.putStringSet("selectedEvents", new HashSet<String>(selectedEvents));
        editor.putStringSet("selectedItineraries", new HashSet<String>(selectedItineraries));
        editor.putStringSet("selectedCategories", new HashSet<String>(selectedCategories));

        editor.commit();
    }


    @Override
    protected NavDrawerActivityConfiguration getNavDrawerConfiguration() {
        view = generateView("View", 100, "menu_view.xml");

        options = generateView("Options", 200, "menu_options.xml");

        NavDrawerItem[] menu = mergeMenus(view, options, categories);

        navDrawerActivityConfiguration = new NavDrawerActivityConfiguration();
        navDrawerActivityConfiguration.setMainLayout(R.layout.act_navdrawer);
        navDrawerActivityConfiguration.setDrawerLayoutId(R.id.drawer_layout);
        navDrawerActivityConfiguration.setLeftDrawerId(R.id.left_drawer);
        navDrawerActivityConfiguration.setNavItems(menu);
        navDrawerActivityConfiguration.setDrawerShadow(R.drawable.drawer_shadow);
        navDrawerActivityConfiguration.setDrawerOpenDesc(R.string.drawer_open);
        navDrawerActivityConfiguration.setDrawerCloseDesc(R.string.drawer_close);
        navDrawerActivityConfiguration.setBaseAdapter(new NavDrawerAdapter(this, R.layout.act_navdrawer_item, menu));
        return navDrawerActivityConfiguration;
    }


    public void openNavDrawer() {
        openDrawer();
    }

    public void closeNavDrawer() {
        closeDrawer();
    }

    private void updateSetSelected(String label, int code) {
        selectItemMine(code, label);
    }


    private void updateSetSelectedCategories(String selectedOptions, int code) {

        for (int i = 0; i < selectedCategories.size(); i++) {
            setItemCheckedCategories(300, selectedCategories.get(i), true);
        }
    }


    private void updateList(NavDrawerItem[] menu) {

        navDrawerActivityConfiguration.setNavItems(menu);
        setAdapterMine(menu);
        updateSetSelected(selectedView, 100);
        updateSetSelected(selectedOptions, 200);
        updateSetSelectedCategories(selectedOptions, 300);
    }

    private NavDrawerItem[] mergeMenus(NavDrawerItem[] view, NavDrawerItem[] options, NavDrawerItem[] categories) {
        if (this.view == null) {
            this.view = view;
        }
        if (this.options == null) {
            this.options = options;
        }
        this.categories = categories;

        NavDrawerItem[] all = org.apache.commons.lang3.ArrayUtils.addAll(this.view, this.options);
        all = org.apache.commons.lang3.ArrayUtils.addAll(all, this.categories);
        return all;
    }

    private NavDrawerItem[] generateView(String header, int code, String file) {
        List<NavDrawerItem> result = new ArrayList<NavDrawerItem>();
        result.add(NavMenuSection.create(code, header));
        for (String string : processXML(file)) {
            code++;
            result.add(NavMenuItem.create(code, string, string, false, this));
        }
        return result.toArray(new NavDrawerItem[result.size()]);
    }

    private NavDrawerItem[] generateViewFromList(String header, int code, List<CategoryDomain> list) {
        List<NavDrawerItem> result = new ArrayList<NavDrawerItem>();
        result.add(NavMenuSection.create(code, header));
        for (CategoryDomain categoryDomain : list) {
            code++;
            result.add(NavMenuItem.create(code, categoryDomain.getName(), categoryDomain.getName(), false, this));
        }
        return result.toArray(new NavDrawerItem[result.size()]);
    }

    private List<String> processXML(String file) {
        List<String> result = new ArrayList<String>();
        Document doc = XmlParser.processXMLAssets(getApplicationContext(), file);
        NodeList nl = doc.getElementsByTagName("item");
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            result.add(e.getAttribute("title"));
        }
        return result;
    }

    @Override
    protected void onNavItemSelected(int id, String label) {

        if (id > 100 && id < 200) {
            updateSetSelected(label, 100);
            if (label.equalsIgnoreCase(VIEW_MAPS) && !selectedView.equalsIgnoreCase(VIEW_MAPS)) {
                //getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                //      mapsActivity).commit();
                getSupportFragmentManager().beginTransaction().detach(listActivity).commit();
                getSupportFragmentManager().beginTransaction().attach(mapsActivity).commit();
                //performSearch();
            } else if (label.equalsIgnoreCase(VIEW_LIST) && !selectedView.equalsIgnoreCase(VIEW_LIST)) {
                //getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                //      listActivity).commit();
                getSupportFragmentManager().beginTransaction().detach(mapsActivity).commit();
                getSupportFragmentManager().beginTransaction().attach(listActivity).commit();
                //performSearch();
            }
            selectedView = label;

            getDrawerLayout().closeDrawers();

        } else if (id > 200 && id < 300) {
            selectedCategories.clear();
            selectedOptions = label;
            updateSetSelected(label, 200);

            if (selectedOptions.equalsIgnoreCase(STR_PLACE)) {
                selectedCategories = new ArrayList<String>(selectedPlaces);
            } else if (selectedOptions.equalsIgnoreCase(STR_EVENT)) {
                selectedCategories = new ArrayList<String>(selectedEvents);
            } else if (selectedOptions.equalsIgnoreCase(STR_ITINERARIES)) {
                selectedCategories = new ArrayList<String>(selectedItineraries);
            }
            changeOptions();

        } else if (id > 300 && id < 400) {
            selectElementCategories(label);
        }

        saveMenuOptions();
    }


    private void updateCategories() {
        SyncUtils.TriggerRefresh();
    }

    @Override
    protected void performSearch() {
        getPois();
    }

    private void getPois() {

        SharedPreferences userDetails = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        float lat = userDetails.getFloat("latPoint", 0);
        float lng = userDetails.getFloat("lngPoint", 0);

        List<String> selectedCategories = new ArrayList<String>(userDetails.getStringSet("selectedCategories", new HashSet<String>()));

        if (selectedCategories == null || selectedCategories.size() == 0) {
            observerClass.setValue(null);
            return;
        }

        java.util.Collections.sort(selectedCategories);

        Boolean hasPois = mPoi != null && mPoi.size() != 0;
        String hash = getSavedCategories();

        if(hash.equals(selectedOptions+selectedCategories.hashCode()) && hasPois && compareFloat
                (getSavedPoint()[0], lat) && compareFloat(getSavedPoint()[1], lng)) {
            Log.d(TAG, "Already has the information");
            return;
        }

        setProgressBarIndeterminateVisibility(true);

        ParameterList list = new ParameterList();

        try {
            if (selectedCategories.contains("All Categories")) {
                list.add(new Parameter(ParameterTerms.LIMIT, 300));
                list.add(new Parameter(ParameterTerms.COORDS, lat + " " + lng + " " + 400));

            } else {
                list.add(new Parameter(ParameterTerms.LIMIT, 300));
                list.add(new Parameter(ParameterTerms.CATEGORY, selectedCategories));
            }

            if (selectedOptions.equalsIgnoreCase("places")) {
                TourismAPI.getPlaceCategories(this, this, list, "", selectedOptions);
            } else if (selectedOptions.equalsIgnoreCase("events")) {
                list.add(new Parameter(ParameterTerms.TIME, getTimeParam()));
                TourismAPI.getEventCategories(this, this, list, "", selectedOptions);
            } else if (selectedOptions.equalsIgnoreCase("itineraries")) {
                TourismAPI.getItinerariesCategories(this, this, list, "", selectedOptions);
            }
        } catch (InvalidValueException e) {
            e.printStackTrace();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        }
        saveCategories();
        savePoint();

    }

    private void savePoint() {

        SharedPreferences userDetails = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        float latitude = userDetails.getFloat("latPoint", 0);
        float longitude = userDetails.getFloat("lngPoint", 0);

        SharedPreferences preferences = getSharedPreferences("sharedPrefs", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("selectedPointLatToSearch", latitude);
        editor.putFloat("selectedPointLngToSearch", longitude);
        editor.commit();
    }

    private void saveCategories() {
        SharedPreferences preferences = getSharedPreferences("sharedPrefs", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("selectedCategoriesHash", selectedOptions+selectedCategories.hashCode());
        editor.commit();
    }

    private String getSavedCategories() {
        SharedPreferences userDetails = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        return userDetails.getString("selectedCategoriesHash", "");
    }

    private float[] getSavedPoint() {
        SharedPreferences userDetails = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        float latitude = userDetails.getFloat("selectedPointLatToSearch", 0);
        float longitude = userDetails.getFloat("selectedPointLngToSearch", 0);
        float[] arr = {latitude, longitude};
        return  arr;
    }

    private boolean compareFloat(float f1, float f2) {
        float epsilon = 0.000001f;
        return (Math.abs(f1 - f2) < epsilon);
    }

    private String getTimeParam() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncConnPref = sharedPref.getString(SettingsActivity.PREF_MENU_EVENTS_DAYS, "7");

        Calendar c = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateandTime = sdf.format(c.getTime());

        c.add(Calendar.DAY_OF_YEAR, Integer.parseInt(syncConnPref));
        String formattedDate = sdf.format(c.getTime());

        return currentDateandTime + " " + formattedDate;
    }

    public ObservableClass getObservableClass() {
        return observerClass;
    }

    private void selectElementCategories(String label) {

        if (selectedCategories.contains(label)) {
            setItemCheckedCategories(300, label, false);
            selectedCategories.remove(selectedCategories.indexOf(label));
        } else {
            setItemCheckedCategories(300, label, true);
            selectedCategories.add(label);
        }

        if (selectedOptions.equalsIgnoreCase(STR_PLACE)) {
            selectedPlaces = new ArrayList<String>(selectedCategories);
        } else if (selectedOptions.equalsIgnoreCase(STR_EVENT)) {
            selectedEvents = new ArrayList<String>(selectedCategories);
        } else if (selectedOptions.equalsIgnoreCase(STR_ITINERARIES)) {
            selectedItineraries = new ArrayList<String>(selectedCategories);
        }
    }


    private boolean compareCollection(ArrayList<String> one, ArrayList<String> two) {
        if (one.size() != two.size()) {
            return false;
        }
        for (String s : one) {
            if (!two.contains(s)) {
                return false;
            }
        }
        return true;
    }

    private String convertSTRtoDB(String STR) {
        String search = "";

        if (selectedOptions.equalsIgnoreCase(STR_PLACE)) {
            search = DB_PLACE;
        }
        if (selectedOptions.equalsIgnoreCase(STR_EVENT)) {
            search = DB_EVENT;
        }
        if (selectedOptions.equalsIgnoreCase(STR_ITINERARIES)) {
            search = DB_ITINERARIES;
        }
        return search;
    }

    private void changeOptions() {
        mCategories.clear();
        final ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Cursor c = contentResolver.query(
                PoisContract.Category.CONTENT_URI,
                PoisContract.Category.PROJECTION_CATEGORY,
                PoisContract.Category.COLUMN_CATEGORY_OPTION + "= '" + convertSTRtoDB(selectedOptions) + "'",
                null,
                PoisContract.Category.COLUMN_CATEGORY_NAME + " ASC");
        assert c != null;
        mCategories.add(new CategoryDomain("", "", "All Categories"));

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            String name = c.getString(PoisContract.Category.CATEGORY_COLUMN_NAME);
            mCategories.add(new CategoryDomain("", "", name));
        }
        updateList(mergeMenus(null, null, generateViewFromList("Categories", 300, mCategories)));
        c.close();
        setProgressBarIndeterminateVisibility(false);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        CursorLoader cursorLoaderCategories = new CursorLoader(getApplicationContext(),
                PoisContract.Category.CONTENT_URI,
                PoisContract.Category.PROJECTION_CATEGORY,
                PoisContract.Category.COLUMN_CATEGORY_OPTION + " = ' " + convertSTRtoDB(selectedOptions) + "'",
                null,
                null);
        return cursorLoaderCategories;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor arg1) {
        changeOptions();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onResultsFinished(POI poi, int id, String parameterTerm,
                                  String bytesOfMessage) {

        if (id == 1) {

            if (poi == null || ((POIS<POI>) poi).size() == 0) {
                observerClass.setValue(null);

            } else {
                observerClass.setValue((POIS<POI>) poi);
            }
            setProgressBarIndeterminateVisibility(false);

        } else if (id == 2) {

            if (poi == null) {
                return;
            }

            mPoi = (POIS<POI>) poi;
            //for(int i=0; i< mPoi.size(); i++) {
            if (mPoi.size() == 0) {
                Toast.makeText(getApplicationContext(), "No endpoint available for this position. Try in other position please", Toast.LENGTH_SHORT);
                return;
            }
            List<POITermType> links = mPoi.get(0).getLink();

            for (POITermType link : links) {
                if (link.getTerm().equals(Term.LINK_TERM_DESCRIBEDBY.getTerm())) {
                    if (!link.getValue().equals("")) {

                        if (link.getValue().equalsIgnoreCase("http://tourism.citysdk.cm-lisboa.pt/resources")) {
                            TourismAPI.setURL(this, link.getValue(), "pt-PT");
                        } else if (link.getValue().equalsIgnoreCase("http://citysdk.dmci.hva.nl/CitySDK/resources")) {
                            TourismAPI.setURL(this, link.getValue(), "nl-NL");
                        } else if (link.getValue().equalsIgnoreCase("http://citysdk.inroma.roma.it/CitySDK/resources")) {
                            TourismAPI.setURL(this, link.getValue(), "it-IT");
                        } else if (link.getValue().equalsIgnoreCase("http://tourism.citysdk.lamia-city.gr/resources")) {
                            TourismAPI.setURL(this, link.getValue(), "el-GR");
                        }

                        SyncUtils.TriggerRefresh();

                    }
                }
            }
        }
    }

    public void changeEndpoint(LatLng point) {

        SharedPreferences userDetails = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        float latitude = userDetails.getFloat("latPoint", 0);
        float longitude = userDetails.getFloat("lngPoint", 0);
        LatLng oldPoint = new LatLng(latitude, longitude);

        double lat = point.latitude;
        double lng = point.longitude;

        SharedPreferences preferences = getSharedPreferences("sharedPrefs", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("latPoint", (float) lat);
        editor.putFloat("lngPoint", (float) lng);
        editor.commit();

        LatLng newPoint = new LatLng(lat, lng);


        ParameterList list = new ParameterList();
        try {
            list.add(new Parameter(ParameterTerms.LIMIT, -1));
            list.add(new Parameter(ParameterTerms.COORDS, lat + " " + lng));
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        } catch (InvalidValueException e) {
            e.printStackTrace();
        }
        if ((oldPoint.latitude == 0 && oldPoint.longitude == 0) || distanceToPoints(oldPoint, newPoint) > 10000) {
            setProgressBarIndeterminateVisibility(true);

            TourismAPI.getEndpoint((OnResultsListener) this, list);
            updateCategories();
        }
    }

    public double distanceToPoints(LatLng p1, LatLng p2) {
        double R = 6371;
        double lat1 = p1.latitude;
        double lat2 = p2.latitude;
        double lon1 = p1.longitude;
        double lon2 = p2.longitude;
        double x = (lon2 - lon1) * Math.cos((lat1 + lat2) / 2);
        double y = (lat2 - lat1);
        return Math.sqrt(x * x + y * y) * R;
    }

    public class ObservableClass extends Observable {
        private POIS<POI> poi;

        public POIS<POI> getValue() {
            return poi;
        }

        public void setValue(POIS<POI> poi) {
            this.poi = poi;
            setChanged();
            notifyObservers();
        }
    }


}