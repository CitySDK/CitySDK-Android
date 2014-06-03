package com.citysdk.demo.activities;

import com.google.android.gms.maps.model.LatLng;

import com.citysdk.demo.R;
import com.citysdk.demo.listener.OnResultsListener;
import com.citysdk.demo.utils.TourismAPI;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.codeforamerica.open311.facade.APIWrapper;
import org.codeforamerica.open311.facade.APIWrapperFactory;
import org.codeforamerica.open311.facade.Format;
import org.codeforamerica.open311.facade.data.POSTServiceRequestResponse;
import org.codeforamerica.open311.facade.data.operations.POSTServiceRequestData;
import org.codeforamerica.open311.facade.exceptions.APIWrapperException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import citysdk.tourism.client.exceptions.InvalidParameterException;
import citysdk.tourism.client.exceptions.InvalidValueException;
import citysdk.tourism.client.parser.DataReader;
import citysdk.tourism.client.parser.data.GeometryContent;
import citysdk.tourism.client.parser.data.ImageContent;
import citysdk.tourism.client.parser.data.LineContent;
import citysdk.tourism.client.parser.data.LocationContent;
import citysdk.tourism.client.parser.data.PointContent;
import citysdk.tourism.client.parser.data.PolygonContent;
import citysdk.tourism.client.poi.base.POITermType;
import citysdk.tourism.client.poi.single.POI;
import citysdk.tourism.client.poi.single.PointOfInterest;
import citysdk.tourism.client.requests.Parameter;
import citysdk.tourism.client.requests.ParameterList;
import citysdk.tourism.client.terms.ParameterTerms;
import citysdk.tourism.client.terms.Term;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.Telephone;
import ezvcard.property.Url;

public class ShowMoreInfoActivity extends Activity implements OnResultsListener {

    private static final String STATE_POSITION = "STATE_POSITION";

    DisplayImageOptions options;

    ViewPager pager;

    int positionGeral = 1;

    TextView imageNumber;

    String name;
    String url;
    String idMoreInfo;

    List<GeometryContent> pos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_show_more_info);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext()).build();
        ImageLoader.getInstance().init(config);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .resetViewBeforeLoading(true)
                .cacheOnDisc(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        pager = (ViewPager) findViewById(R.id.pager);
        imageNumber = (TextView) findViewById(R.id.act_show_more_info_text_image_number);

    }

    @Override
    public void onResume() {
        super.onResume();

        Intent i = getIntent();
        String id = i.getStringExtra("id");
        String base = i.getStringExtra("base");
        url = base+id;
        idMoreInfo = id;
        try {
            ParameterList list = new ParameterList();
            list.add(new Parameter(ParameterTerms.ID, id));
            list.add(new Parameter(ParameterTerms.BASE, base));
            TourismAPI.getSinglePoi(getApplicationContext(), this, list);
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        } catch (InvalidValueException e) {
            e.printStackTrace();
        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                try {
//                    APIWrapper wrapper = new APIWrapperFactory("http://web4.cm-lisboa.pt/citySDK/v1",
//                            Format.XML).build();
//                    for (Service s : wrapper.getServiceList()) {
//                        System.out.println("    -     " + s.getServiceCode());
//                    }
//                    APIWrapper wrapperPost = new APIWrapperFactory("http://web4.cm-lisboa.pt/citySDK/v1",Format.XML).setApiKey("***REMOVED***").build();
//
//                    POSTServiceRequestData psrd = new POSTServiceRequestData("652", 38.715209f ,-9.140453f, null);
//
//                    psrd.setDescription("Teste Teste Teste Teste Teste Teste Teste Teste Teste Teste");
//                    psrd.setLatLong(38.715209f, -9.140453f);
//                    POSTServiceRequestResponse response =  wrapperPost.postServiceRequest(psrd);
//                    System.out.println("~~~~"+ response.getServiceRequestId());
//                } catch (APIWrapperException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.moreinfo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_report:
                showReportDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void showReportDialog() {
        Context mContext = getApplicationContext();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_report, (ViewGroup) findViewById(R.id.dialog_report_layout));

        if(StringUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "problem in name", Toast.LENGTH_SHORT);
        }

        if (pos.size() == 0) {
            Toast.makeText(getApplicationContext(), "has no geometries", Toast.LENGTH_SHORT);
        }

        ((TextView) layout.findViewById(R.id.dialog_report_name)).setText(name);
        final TextView description = ((TextView) layout.findViewById(R.id.dialog_report_input));
        final LatLng latLng;

        int numGeo = pos.get(0).getNumGeo();

        if (numGeo == 1) {
            PointContent content = (PointContent) pos.get(0);
            LocationContent location = content.getLocation();
            latLng = new LatLng((Float.parseFloat(location.getLatitude())), (Float.parseFloat(location.getLongitude())));

        }
        else if (numGeo == 2) {
            LineContent content = (LineContent) pos.get(0);
            latLng = new LatLng((Float.parseFloat(content.getPointOne().getLatitude())), (Float.parseFloat(content.getPointOne().getLongitude())));

        }
        else if (numGeo > 2) {
            PolygonContent content = (PolygonContent) pos.get(0);
            latLng =  new LatLng((Float.parseFloat(content.getValues().get(0).getLatitude())),(Float.parseFloat(content.getValues().get(0).getLongitude())));

        }
        else {
            latLng = new LatLng(0,0);
        }

        new AlertDialog.Builder(this)
                .setView(layout)
                .setTitle(R.string.reportProblem)
                .setPositiveButton(R.string.report, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String desc = "ID: "+idMoreInfo+" URL: "+url + " Description: "+description.getText().toString();
                        if(!StringUtils.isEmpty(desc)) {

                            sendReport(desc, latLng);
                        } else {
                            Toast.makeText(getApplicationContext(), "You must provide a description", Toast.LENGTH_SHORT);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    } })
                .show();
    }

    private void sendReport(final String description, final LatLng latlng) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /*
                    APIWrapper wrapper = new APIWrapperFactory("http://web4.cm-lisboa.pt/citySDK/v1",
                            Format.XML).build();
                    for (Service s : wrapper.getServiceList()) {
                        System.out.println("    -     " + s.getServiceCode());
                    }
                    */
                    APIWrapper wrapperPost = new APIWrapperFactory("http://web4.cm-lisboa.pt/citySDK/v1",Format.XML).setApiKey("***REMOVED***").build();
                    POSTServiceRequestData psrd = new POSTServiceRequestData("652", (float)latlng.latitude , (float)latlng.longitude, null);
                    psrd.setDescription(description);
                    //psrd.setLatLong(38.715209f, -9.140453f);
                    POSTServiceRequestResponse response =  wrapperPost.postServiceRequest(psrd);
                    System.out.println("~~~~"+ response.getServiceRequestId());
                } catch (APIWrapperException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onResultsFinished(POI poi, int id, String parameterTerm, String bytes) {
        @SuppressWarnings("unchecked")
        PointOfInterest pois = (PointOfInterest) poi;

        if (pois == null) {
            return;
        }
        Activity activity = this;

        Locale locale = (Locale) TourismAPI.getURL(getApplicationContext())[1];

        name = DataReader.getLabel(pois, Term.LABEL_TERM_PRIMARY, locale);

        pos = DataReader.getLocationGeometry(poi, Term.POINT_TERM_ENTRANCE);
        if (pos.size() == 0) {
            pos = DataReader.getLocationGeometry(poi, Term.POINT_TERM_CENTER);
            if (pos.size() == 0) {
                pos = DataReader.getLocationGeometry(poi, Term.POINT_TERM_NAVIGATION_POINT);
            }
        }

        String contacts = DataReader.getContacts(pois);

        String categories = StringUtils
                .join(DataReader.getCategories(pois, locale).toArray(), "\n");

        String description = DataReader.getDescription(pois, locale);

        ((TextView) activity.findViewById(R.id.act_show_more_info_text_title)).setText(name);
        ((TextView) activity.findViewById(R.id.act_show_more_info_text_category))
                .setText(categories);

        if (contacts != null && contacts != "") {

            VCard vcard = Ezvcard.parse(contacts).first();
            String separator = ", ";
            boolean allDisabled = true;

            if (vcard == null || vcard.getTelephoneNumbers() == null
                    || vcard.getTelephoneNumbers().size() == 0
                    || vcard.getTelephoneNumbers().get(0).getText() == "") {
                activity.findViewById(R.id.act_show_more_info_text_contactos_tel)
                        .setVisibility(View.GONE);
                activity.findViewById(R.id.act_show_more_info_text_contactos_tel_input)
                        .setVisibility(View.GONE);
            } else {
                allDisabled = false;
                activity.findViewById(R.id.act_show_more_info_text_contactos_tel)
                        .setVisibility(View.VISIBLE);
                activity.findViewById(R.id.act_show_more_info_text_contactos_tel_input)
                        .setVisibility(View.VISIBLE);
                StringBuilder sb = new StringBuilder();
                for (Telephone t : vcard.getTelephoneNumbers()) {
                    sb.append(separator).append(t.getText());
                }
                String result = sb.substring(separator.length());
                ((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_tel_input))
                        .setText(result);
            }

            if (vcard == null || vcard.getEmails() == null || vcard.getEmails().size() == 0
                    || vcard.getEmails().get(0).getValue() == "") {
                activity.findViewById(R.id.act_show_more_info_text_contactos_mail)
                        .setVisibility(View.GONE);
                activity.findViewById(R.id.act_show_more_info_text_contactos_mail_input)
                        .setVisibility(View.GONE);
            } else {
                allDisabled = false;
                activity.findViewById(R.id.act_show_more_info_text_contactos_mail)
                        .setVisibility(View.VISIBLE);
                activity.findViewById(R.id.act_show_more_info_text_contactos_mail_input)
                        .setVisibility(View.VISIBLE);
                StringBuilder sb = new StringBuilder();
                for (Email t : vcard.getEmails()) {
                    sb.append(separator).append(t.getValue());
                }
                String result = sb.substring(separator.length());
                ((TextView) activity
                        .findViewById(R.id.act_show_more_info_text_contactos_mail_input))
                        .setText(result);
            }

            if (vcard == null || vcard.getUrls() == null || vcard.getUrls().size() == 0
                    || vcard.getUrls().get(0).getValue() == "") {
                activity.findViewById(R.id.act_show_more_info_text_contactos_url)
                        .setVisibility(View.GONE);
                activity.findViewById(R.id.act_show_more_info_text_contactos_url_input)
                        .setVisibility(View.GONE);
            } else {
                allDisabled = false;
                activity.findViewById(R.id.act_show_more_info_text_contactos_url)
                        .setVisibility(View.VISIBLE);
                activity.findViewById(R.id.act_show_more_info_text_contactos_url_input)
                        .setVisibility(View.VISIBLE);
                StringBuilder sb = new StringBuilder();
                for (Url t : vcard.getUrls()) {
                    sb.append(separator).append(t.getValue());
                }
                String result = sb.substring(separator.length());
                ((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_url_input))
                        .setText(result);
            }

            if (vcard == null || vcard.getAddresses() == null || vcard.getAddresses().size() == 0
                    || vcard.getAddresses().get(0).getExtendedAddress() == "") {
                activity.findViewById(R.id.act_show_more_info_text_contactos_address)
                        .setVisibility(View.GONE);
                activity.findViewById(R.id.act_show_more_info_text_contactos_address_input)
                        .setVisibility(View.GONE);
            } else {
                allDisabled = false;
                activity.findViewById(R.id.act_show_more_info_text_contactos_address)
                        .setVisibility(View.VISIBLE);
                activity.findViewById(R.id.act_show_more_info_text_contactos_address_input)
                        .setVisibility(View.VISIBLE);
                StringBuilder sb = new StringBuilder();
                for (Address t : vcard.getAddresses()) {
                    sb.append(separator).append(t.getExtendedAddress());
                }
                String result = sb.substring(separator.length());
                ((TextView) activity
                        .findViewById(R.id.act_show_more_info_text_contactos_address_input))
                        .setText(result);
            }
            if (allDisabled = true) {
                activity.findViewById(R.id.act_show_more_info_text_contacts)
                        .setVisibility(View.GONE);
            }
            activity.findViewById(R.id.act_show_more_info_text_contacts)
                    .setVisibility(View.VISIBLE);

        } else {
            activity.findViewById(R.id.act_show_more_info_text_contacts).setVisibility(View.GONE);
        }

        if (pois.getTime() == null || pois.getTime().size() == 0) {
            activity.findViewById(R.id.act_show_more_info_text_sch).setVisibility(View.GONE);
        } else {
            activity.findViewById(R.id.act_show_more_info_text_sch).setVisibility(View.VISIBLE);

            for (POITermType poiTermType : pois.getTime()) {
                if (poiTermType.getType() == null || !poiTermType.getType()
                        .equalsIgnoreCase("text/icalendar")) {
                    activity.findViewById(R.id.act_show_more_info_text_start_end)
                            .setVisibility(View.GONE);
                    activity.findViewById(R.id.act_show_more_info_text_all)
                            .setVisibility(View.VISIBLE);
                    ((TextView) activity.findViewById(R.id.act_show_more_info_text_all))
                            .setText(poiTermType.getValue());
                } else if (poiTermType.getType().equalsIgnoreCase("text/icalendar")) {
                    activity.findViewById(R.id.act_show_more_info_text_start_end)
                            .setVisibility(View.VISIBLE);
                    activity.findViewById(R.id.act_show_more_info_text_all)
                            .setVisibility(View.GONE);

                    StringReader sin = new StringReader(poiTermType.getValue());
                    CalendarBuilder builder = new CalendarBuilder();
                    try {
                        Calendar calendar = builder.build(sin);

//                        for (Iterator i = calendar.getComponents().iterator(); i.hasNext(); ) {
//                            Component component = (Component) i.next();
//                            System.out.println("Component [" + component.getName() + "]");
//
//                            for (Iterator j = component.getProperties().iterator(); j.hasNext(); ) {
//                                Property property = (Property) j.next();
//                                System.out.println(
//                                        "Property [" + property.getName() + ", " + property
//                                                .getValue() + "]");
//                            }
//                        }

                        String start = calendar.getComponent("VEVENT").getProperty("DTSTART")
                                .getValue();
                        String end = calendar.getComponent("VEVENT").getProperty("DTEND")
                                .getValue();

                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
                            SimpleDateFormat output = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            Date dstart = sdf.parse(start);
                            Date dend = sdf.parse(end);
                            String startDate = output.format(dstart);
                            String endDate = output.format(dend);

                            ((TextView) activity
                                    .findViewById(R.id.act_show_more_info_text_start_input))
                                    .setText(startDate);
                            ((TextView) activity
                                    .findViewById(R.id.act_show_more_info_text_end_input))
                                    .setText(endDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (ParserException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

        if (description == null || description == "") {
            activity.findViewById(R.id.act_show_more_info_text_desc).setVisibility(View.GONE);
        } else {
            activity.findViewById(R.id.act_show_more_info_text_desc).setVisibility(View.VISIBLE);
            ((TextView) activity.findViewById(R.id.act_show_more_info_text_description))
                    .setText(description);
        }

        final List<ImageContent> imageContent = DataReader.getImagesUri(pois);
        int pagerPosition = 0;

        if (imageContent != null && imageContent.size() != 0) {
            String[] arr = new String[imageContent.size()];
            for (int i = 0; i < imageContent.size(); i++) {
                arr[i] = imageContent.get(i).getContent();
            }

            pager.setAdapter(new ImagePagerAdapter(arr));
            imageNumber.setText(positionGeral + "/" + imageContent.size());
        } else {
            imageNumber.setText(0 + "/" + 0);
            String imageUri = "drawable://" + R.drawable.ic_empty;
            String[] arr = {imageUri};
            pager.setAdapter(new ImagePagerAdapter(arr));
        }

        pager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        positionGeral = position + 1;
                        imageNumber.setText(positionGeral + "/" + imageContent.size());
                    }
                }
        );
    }


    private class ImagePagerAdapter extends PagerAdapter {

        private String[] images;

        private LayoutInflater inflater;

        ImagePagerAdapter(String[] images) {
            this.images = images;
            inflater = getLayoutInflater();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
            assert imageLayout != null;

            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);

            ImageLoader.getInstance().displayImage(images[position], imageView, options,
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            spinner.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view,
                                FailReason failReason) {
                            String message = null;
                            switch (failReason.getType()) {
                                case IO_ERROR:
                                    message = "Input/Output error";
                                    break;
                                case DECODING_ERROR:
                                    message = "Image can't be decoded";
                                    break;
                                case NETWORK_DENIED:
                                    message = "Downloads are denied";
                                    break;
                                case OUT_OF_MEMORY:
                                    message = "Out Of Memory error";
                                    break;
                                case UNKNOWN:
                                    message = "Unknown error";
                                    break;
                            }
                            //Toast.makeText(ShowMoreInfoActivity.this, message, Toast.LENGTH_SHORT).show();

                            spinner.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view,
                                Bitmap loadedImage) {
                            spinner.setVisibility(View.GONE);
                        }
                    });

            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}
