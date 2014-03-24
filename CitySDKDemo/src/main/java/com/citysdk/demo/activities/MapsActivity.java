package com.citysdk.demo.activities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import citysdk.tourism.client.parser.DataReader;
import citysdk.tourism.client.poi.lists.POIS;
import citysdk.tourism.client.poi.single.POI;
import citysdk.tourism.client.terms.Term;
import citysdk.tourism.client.parser.data.GeometryContent;
import citysdk.tourism.client.parser.data.LineContent;
import citysdk.tourism.client.parser.data.LocationContent;
import citysdk.tourism.client.parser.data.PointContent;
import citysdk.tourism.client.parser.data.PolygonContent;
import com.citysdk.demo.R;
import com.citysdk.demo.maps.Marker;
import com.citysdk.demo.utils.TourismAPI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class MapsActivity extends Fragment implements Observer, ConnectionCallbacks, OnConnectionFailedListener, ClusterManager.OnClusterClickListener<Marker>, ClusterManager.OnClusterItemClickListener<Marker>, OnMapLongClickListener {

	public static final String PREFS_NAME = "MyPrefsFile";
	private GoogleMap map;
	private ClusterManager<Marker> mClusterManager;
	private ActNavigationDrawer actNavigationDrawer;
	private SupportMapFragment fragment;
	private LocationClient locationClient;
	private com.google.android.gms.maps.model.Marker markerMyPosition;
	private boolean myPositionFlag = false;

	public MapsActivity(ActNavigationDrawer actNavigationDrawer) {
		this.actNavigationDrawer = actNavigationDrawer;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.act_maps, container, false);
		actNavigationDrawer.getObservableClass().addObserver(this);
		return fragmentView;
	}	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager fm = getChildFragmentManager();
		fragment = (SupportMapFragment) fm.findFragmentById(R.id.act_maps_mapfragment);
		if (fragment == null) {
			fragment = SupportMapFragment.newInstance();
			fm.beginTransaction().replace(R.id.act_maps_mapfragment, fragment).commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		setupMap();
		if(actNavigationDrawer.getObservableClass().getValue() != null && actNavigationDrawer.getObservableClass().getValue().size()!=0) {
			showMarker(actNavigationDrawer.getObservableClass().getValue());
		}

	}

	@Override
	public void onPause() {
		super.onPause();

		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		if(map != null) { 
			editor.putFloat("zoom", map.getCameraPosition().zoom);
			editor.putFloat("tilt", map.getCameraPosition().tilt);
			editor.putFloat("latitude", (float)map.getCameraPosition().target.latitude);
			editor.putFloat("longitude", (float)map.getCameraPosition().target.longitude);
			editor.putFloat("bearing", map.getCameraPosition().bearing);
		}
		editor.commit();

		locationClient.disconnect();
	}	

	@Override
	public void onDetach() {
		super.onDetach();

		try {
			Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
			childFragmentManager.setAccessible(true);
			childFragmentManager.set(this, null);

		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
	}

	@Override
	public void onConnected(Bundle connectionHint) {

		Location loc = locationClient.getLastLocation();
		if(loc == null) {
			return;
		}
		LatLng coord = new LatLng(loc.getLatitude(), loc.getLongitude());

		if(!myPositionFlag) {
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coord, 17);
			map.animateCamera(cameraUpdate);
		}
	}

	@Override
	public void onDisconnected() {
	}	

	private void setupMap() {

		//		if(map != null && mClusterManager != null && locationClient != null) {
		//			return;
		//		}
		SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, 0);

		float zoom = prefs.getFloat("zoom", 17);
		float tilt = prefs.getFloat("tilt", 30);
		float latitude = prefs.getFloat("latitude", 0);
		float longitude = prefs.getFloat("longitude", 0);
		float bearing = prefs.getFloat("bearing", 90);

		map = fragment.getMap();	
		if (mClusterManager == null) {
			mClusterManager = new ClusterManager<Marker>(getActivity(), map);
		}
		map.setMyLocationEnabled(true);
		map.setIndoorEnabled(true);


		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

		map.setOnMarkerClickListener(mClusterManager);
		map.setOnCameraChangeListener(mClusterManager);
		map.setOnMapLongClickListener(this);
		mClusterManager.setOnClusterClickListener(this);
		mClusterManager.setOnClusterItemClickListener(this);
		mClusterManager.setRenderer(new MarkerRenderer());


		if(latitude == 0 && longitude == 0) {
			myPositionFlag = false;
		} else {
			myPositionFlag = true;
			CameraPosition cameraPosition = new CameraPosition.Builder()
			.target(new LatLng(latitude, longitude))
			.zoom(zoom)
			.bearing(bearing)
			.tilt(tilt)
			.build();  
			map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

		}
		locationClient = new LocationClient(getActivity(), this, this);
		locationClient.connect();		

		//		Location loc = locationClient.getLastLocation();
		//		LatLng coord = new LatLng(loc.getLatitude(), loc.getLongitude());
		//
		//		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coord, 17);
		//		map.animateCamera(cameraUpdate);
		//		locationClient.disconnect();

	}


	public void clearMarker() {
		if (fragment != null) {
			map = fragment.getMap();
			if(map != null) {
				map.clear();
			}
			if(mClusterManager != null) {
				mClusterManager.clearItems();
			}
		}
	}


	@Override
	public void onMapLongClick(final LatLng point) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle("Search");
		builder.setMessage("Do you want to search for information around here?");

		builder.setPositiveButton("Pick Category", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				drawSearchPoint(point);
				actNavigationDrawer.changeEndpoint(point);
				actNavigationDrawer.openNavDrawer();

			}
		}); 			

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		builder.show();
	}


	public void showMarker(POIS<POI> pois) {

		if(fragment == null || fragment.getMap() == null) {
			return;
		}

		if(pois == null || pois.size() == 0) {
			clearMarker();
			return;
		}

		if(mClusterManager == null) {
			setupMap();
		}


		clearMarker();
		drawSearchPoint(null);
		Locale locale = (Locale) TourismAPI.getURL(getActivity().getApplicationContext())[1];

		for(int i = 0 ; i < pois.size() ; i++) {
			POI poi = pois.get(i);
			try {
				List<LocationDetails> latlng = getLocation(poi);
				if (latlng == null || latlng.size() == 0) {
					continue;
				}
				for(LocationDetails point : latlng) {
					if(point.getType() == 1) {
						LatLng pointToWrite = point.getLatLngList().get(0);
						mClusterManager.addItem(new Marker(pointToWrite.latitude, pointToWrite.longitude, DataReader.getLabel(poi, Term.LABEL_TERM_PRIMARY, locale), DataReader.getCategories(poi, locale).get(0), poi.getId(), poi.getBase()));
					} else if(point.getType() == 2){
						PolylineOptions rectOptions = new PolylineOptions();
						rectOptions.width(3);
						for(LatLng pointToWrite : point.getLatLngList()) {
							rectOptions.add(pointToWrite);
						}
						LatLng latlngToWrite = centroPosition(point.getLatLngList());
						mClusterManager.addItem(new Marker(latlngToWrite.latitude, latlngToWrite.longitude, DataReader.getLabel(poi, Term.LABEL_TERM_PRIMARY, locale), DataReader.getCategories(poi, locale).get(0), poi.getId(), poi.getBase()));

						map.addPolyline(rectOptions);

					} else if(point.getType() == 3) {
						PolylineOptions rectOptions = new PolylineOptions();
						rectOptions.width(2f);
						for(LatLng pointToWrite : point.getLatLngList()) {
							rectOptions.add(pointToWrite);
						}
						LatLng latlngToWrite = centroPosition(point.getLatLngList());
						mClusterManager.addItem(new Marker(latlngToWrite.latitude, latlngToWrite.longitude, DataReader.getLabel(poi, Term.LABEL_TERM_PRIMARY, locale), DataReader.getCategories(poi, locale).get(0), poi.getId(), poi.getBase()));

						map.addPolyline(rectOptions);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		mClusterManager.cluster();
	}

	private List<LocationDetails> getLocation(POI poi) throws Exception {

		java.util.List<GeometryContent> list = DataReader.getLocationGeometry(poi, Term.POINT_TERM_ENTRANCE);
		if(list.size() == 0) {
			list = DataReader.getLocationGeometry(poi, Term.POINT_TERM_CENTER);
			if(list.size() == 0) {
				list = DataReader.getLocationGeometry(poi, Term.POINT_TERM_NAVIGATION_POINT);
			}
		}

		if (list.size() == 0)
			throw new Exception("POI " + poi.getId() + " has no geometries");

		List<LocationDetails> listLatLng = new ArrayList<LocationDetails>();

		for(GeometryContent gc : list) {
			int numGeo = gc.getNumGeo();	

			if (numGeo == 1) {
				LocationDetails locationDetails = new LocationDetails(1);
				PointContent content = (PointContent) gc;
				LocationContent location = content.getLocation();
				locationDetails.addLatLngList(new LatLng((Float.parseFloat(location.getLatitude())), (Float.parseFloat(location.getLongitude()))));
				listLatLng.add(locationDetails);
			}
			if (numGeo == 2) {
				LocationDetails locationDetails = new LocationDetails(2);
				LineContent content = (LineContent) gc;
				locationDetails.addLatLngList(new LatLng((Float.parseFloat(content.getPointOne().getLatitude())), (Float.parseFloat(content.getPointOne().getLongitude()))));
				locationDetails.addLatLngList(new LatLng((Float.parseFloat(content.getPointTwo().getLatitude())), (Float.parseFloat(content.getPointTwo().getLongitude()))));
				listLatLng.add(locationDetails);
			}

			if (numGeo > 2) {
				LocationDetails locationDetails = new LocationDetails(3);
				PolygonContent content = (PolygonContent) gc;
				for( LocationContent locationContent : content.getValues()) {
					locationDetails.addLatLngList(new LatLng((Float.parseFloat(locationContent.getLatitude())), (Float.parseFloat(locationContent.getLongitude()))));
				}
				listLatLng.add(locationDetails);
			}
		}

		return listLatLng;
	}

	private  LatLng centroPosition(List<LatLng> points) {
		double[] centroid = { 0.0, 0.0 };

		for (int i = 0; i < points.size(); i++) {
			centroid[0] += points.get(i).latitude;
			centroid[1] += points.get(i).longitude;
		}

		int totalPoints = points.size();
		centroid[0] = centroid[0] / totalPoints;
		centroid[1] = centroid[1] / totalPoints;

		return new LatLng(centroid[0], centroid[1]);
	}

	@Override
	public boolean onClusterClick(Cluster<Marker> cluster) {

		//Marker[] list = (Marker[]) cluster.getItems().toArray();

		CharSequence[] arr = new CharSequence[cluster.getSize()];

		final Marker[] list = cluster.getItems().toArray(new Marker[cluster.getItems().size()]);
		for(int i = 0; i< list.length; i++) {
			arr[i] = list[i].getName();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("More Information")
		.setItems(arr, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				callMoreInfo(list[which]);

			}
		});
		builder.show();
		//	    cluster.getItems().
		//		for(Marker marker : cluster.getItems()) {
		//			System.out.println("--> "+marker.getName());
		//		}
		return true;
	}

	@Override
	public boolean onClusterItemClick(Marker item) {
		//Toast.makeText(getActivity(), item.getCategory() + "\n" + item.getName(), Toast.LENGTH_SHORT).show();
		callMoreInfo(item);
		return true;
	}

	private void callMoreInfo(Marker item) {
		Intent i = new Intent(getActivity(), ShowMoreInfoActivity.class);
		i.putExtra("id", item.getId());
		i.putExtra("base", item.getBase());
		i.putExtra("name", item.getName());
		i.putExtra("category", item.getCategory());
		startActivity(i);
	}


	private class MarkerRenderer extends DefaultClusterRenderer<Marker> {
		private final IconGenerator mIconGenerator = new IconGenerator(getActivity());
		//private final IconGenerator mClusterIconGenerator = new IconGenerator(getActivity());

		private String mTextName = "";

		public MarkerRenderer() {
			super(getActivity(), map, mClusterManager);
			//
			//			View multiProfile = getActivity().getLayoutInflater().inflate(R.layout.act_maps_marker, null);
			//			TextView name = (TextView) multiProfile.findViewById(R.id.act_maps_marker_name);
			//			name.setText(mTextName);
			//			name.setWidth(100);
			//			name.setHeight(20);			
			//			
			//			mIconGenerator.setContentView(multiProfile);
			//			mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);
			//
			//			mImageView = new ImageView(getApplicationContext());
			//			mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
			//			mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
			//			int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
			//			mImageView.setPadding(padding, padding, padding, padding);
			//			mIconGenerator.setContentView(mImageView);
		}

		@Override
		protected void onBeforeClusterItemRendered(Marker person, MarkerOptions markerOptions) {
			// Draw a single person.
			// Set the info window to show their name.
			//			mImageView.setImageResource(person.profilePhoto);
			//			Bitmap icon = mIconGenerator.makeIcon();
			//markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);

			mTextName = person.getName();


			View multiProfile = getActivity().getLayoutInflater().inflate(R.layout.act_maps_marker, null);
			TextView name = (TextView) multiProfile.findViewById(R.id.act_maps_marker_name);
			name.setText(mTextName);
			//			name.setWidth(100);
			//			name.setHeight(20);
			mIconGenerator.setContentView(multiProfile);

			Bitmap icon = mIconGenerator.makeIcon();
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(mTextName);
		}

		@Override
		protected boolean shouldRenderAsCluster(Cluster cluster) {
			// Always render clusters.
			return cluster.getSize() > 1;
		}
	}

	private void drawSearchPoint(LatLng point) {

		LatLng pointToAdd;
		if(point == null) {
			SharedPreferences userDetails = getActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);		
			float lat = userDetails.getFloat("latPoint", 0);
			float lng = userDetails.getFloat("lngPoint", 0);
			pointToAdd = new LatLng(lat, lng);
		} else {
			pointToAdd = point;
		}

		if (markerMyPosition != null) {
			markerMyPosition.remove();
		}
		markerMyPosition = map.addMarker(new MarkerOptions()
		.position(pointToAdd).draggable(false).visible(true));
	}

	@Override
	public void update(Observable observable, Object data) {
		POIS<POI> poi = actNavigationDrawer.getObservableClass().getValue();
		if( poi == null) {
			clearMarker();
		} else {
			showMarker(poi);	
		}
	}
}
