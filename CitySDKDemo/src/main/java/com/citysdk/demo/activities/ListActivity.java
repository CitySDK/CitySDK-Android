package com.citysdk.demo.activities;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.citysdk.demo.R;
import com.citysdk.demo.maps.Marker;
import com.citysdk.demo.utils.TourismAPI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import citysdk.tourism.client.parser.DataReader;
import citysdk.tourism.client.parser.data.GeometryContent;
import citysdk.tourism.client.parser.data.LineContent;
import citysdk.tourism.client.parser.data.LocationContent;
import citysdk.tourism.client.parser.data.PointContent;
import citysdk.tourism.client.parser.data.PolygonContent;
import citysdk.tourism.client.poi.lists.POIS;
import citysdk.tourism.client.poi.single.POI;
import citysdk.tourism.client.terms.Term;

public class ListActivity extends Fragment implements Observer {

    private SupportMapFragment mFragment;

    private MarkerAdapter mAdapter;

    private ListView mListView;

    private ActNavigationDrawer actNavigationDrawer;

    public ListActivity(ActNavigationDrawer actNavigationDrawer) {
        this.actNavigationDrawer = actNavigationDrawer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setRetainInstance(true);
        View fragmentView = inflater.inflate(R.layout.act_list, container, false);
        mListView = (ListView) fragmentView.findViewById(R.id.act_list_listview);
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        actNavigationDrawer.getObservableClass().addObserver(this);

        if (actNavigationDrawer.getObservableClass().getValue() != null
                && actNavigationDrawer.getObservableClass().getValue().size() != 0) {
            showMarker(actNavigationDrawer.getObservableClass().getValue());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        actNavigationDrawer.getObservableClass().deleteObserver(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        actNavigationDrawer.getObservableClass().deleteObserver(this);
    }


    public void showMarker(POIS<POI> pois) {
        if (pois == null || pois.size() <= 0) {
            mListView.setAdapter(null);
            return;
        }
        final ArrayList<Marker> lista = new ArrayList<Marker>();

        Locale locale = (Locale) TourismAPI.getURL(getActivity().getApplicationContext())[1];

        for (int i = 0; i < pois.size(); i++) {
            POI poi = pois.get(i);
            try {
                List<LocationDetails> latlng = getLocation(poi);
                if (latlng == null || latlng.size() == 0) {
                    continue;
                }
                for (LocationDetails point : latlng) {
                    if (point.getType() == 1) {
                        LatLng pointToWrite = point.getLatLngList().get(0);
                        lista.add(new Marker(pointToWrite.latitude, pointToWrite.longitude,
                                DataReader.getLabel(poi, Term.LABEL_TERM_PRIMARY, locale),
                                DataReader.getCategories(poi, locale).get(0), poi.getId(),
                                poi.getBase()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        Collection<Marker> items = lista;
        java.util.Collections.sort((List<Marker>) items, new Comparator<Marker>() {
            @Override
            public int compare(Marker lhs, Marker rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        mAdapter = new MarkerAdapter(getActivity().getApplicationContext(),
                (ArrayList<Marker>) items);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {

                Intent i = new Intent(getActivity(), ShowMoreInfoActivity.class);
                i.putExtra("id", lista.get(position).getId());
                i.putExtra("base", lista.get(position).getBase());
                i.putExtra("name", lista.get(position).getName());
                i.putExtra("category", lista.get(position).getCategory());
                startActivity(i);

            }
        });
    }


    private List<LocationDetails> getLocation(POI poi) throws Exception {

        java.util.List<GeometryContent> list = DataReader
                .getLocationGeometry(poi, Term.POINT_TERM_ENTRANCE);

        if (list.size() == 0) {
            throw new Exception("POI " + poi.getId() + " has no geometries");
        }

        List<LocationDetails> listLatLng = new ArrayList<LocationDetails>();

        for (GeometryContent gc : list) {
            int numGeo = gc.getNumGeo();

            if (numGeo == 1) {
                LocationDetails locationDetails = new LocationDetails(1);
                PointContent content = (PointContent) gc;
                LocationContent location = content.getLocation();
                locationDetails.addLatLngList(new LatLng((Float.parseFloat(location.getLatitude())),
                        (Float.parseFloat(location.getLongitude()))));
                listLatLng.add(locationDetails);
            }
            if (numGeo == 2) {
                LocationDetails locationDetails = new LocationDetails(2);
                LineContent content = (LineContent) gc;
                locationDetails.addLatLngList(
                        new LatLng((Float.parseFloat(content.getPointOne().getLatitude())),
                                (Float.parseFloat(content.getPointOne().getLongitude()))));
                locationDetails.addLatLngList(
                        new LatLng((Float.parseFloat(content.getPointTwo().getLatitude())),
                                (Float.parseFloat(content.getPointTwo().getLongitude()))));
                listLatLng.add(locationDetails);
            }

            if (numGeo > 2) {
                LocationDetails locationDetails = new LocationDetails(3);
                PolygonContent content = (PolygonContent) gc;
                for (LocationContent locationContent : content.getValues()) {
                    locationDetails.addLatLngList(
                            new LatLng((Float.parseFloat(locationContent.getLatitude())),
                                    (Float.parseFloat(locationContent.getLongitude()))));
                }
                listLatLng.add(locationDetails);
            }
        }

        return listLatLng;
    }

    @Override
    public void update(Observable observable, Object data) {
        POIS<POI> poi = actNavigationDrawer.getObservableClass().getValue();
        if (poi != null) {
            showMarker(poi);
        }
    }

    public class MarkerAdapter extends ArrayAdapter<Marker> {

        public MarkerAdapter(Context context, ArrayList<Marker> users) {
            super(context, R.layout.act_list_element, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Marker user = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.act_list_element, null);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.secondLine);
            // Populate the data into the template view using the data object
            tvName.setText(user.getName());
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
