package com.citysdk.demo.activities;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

import org.apache.commons.lang3.StringUtils;
import com.citysdk.demo.R;
import com.citysdk.demo.listener.OnResultsListener;
import com.citysdk.demo.utils.TourismAPI;

import citysdk.tourism.client.exceptions.InvalidParameterException;
import citysdk.tourism.client.exceptions.InvalidValueException;
import citysdk.tourism.client.parser.DataReader;
import citysdk.tourism.client.parser.data.ImageContent;
import citysdk.tourism.client.poi.base.POITermType;
import citysdk.tourism.client.poi.single.POI;
import citysdk.tourism.client.poi.single.PointOfInterest;
import citysdk.tourism.client.requests.Parameter;
import citysdk.tourism.client.requests.ParameterList;
import citysdk.tourism.client.terms.ParameterTerms;
import citysdk.tourism.client.terms.Term;


import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.FailReason;

import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.Telephone;
import ezvcard.property.Url;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ShowMoreInfoActivity extends Activity implements OnResultsListener {

	private static final String STATE_POSITION = "STATE_POSITION";

	DisplayImageOptions options;

	ViewPager pager;
	int positionGeral = 1;
	TextView imageNumber;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_show_more_info);

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
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
		imageNumber = (TextView)  findViewById(R.id.act_show_more_info_text_image_number);

	}	

	@Override
	public void onResume() {
		super.onResume();

		Intent i = getIntent();
		String id = i.getStringExtra("id");
		String base = i.getStringExtra("base");
		String name= i.getStringExtra("name");
		String category = i.getStringExtra("category");

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
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResultsFinished(POI poi, int id, String parameterTerm, String bytes) {
		@SuppressWarnings("unchecked")
		PointOfInterest pois = (PointOfInterest)poi;

		if(pois == null) {
			return;
		}
		Activity activity = this;

		Locale locale = (Locale) TourismAPI.getURL(getApplicationContext())[1];

		String name = DataReader.getLabel(pois, Term.LABEL_TERM_PRIMARY, locale);

		String contacts = DataReader.getContacts(pois);


		String categories = StringUtils.join(DataReader.getCategories(pois, locale).toArray(),"\n");

		String description = DataReader.getDescription(pois, locale);


		((TextView) activity.findViewById(R.id.act_show_more_info_text_title)).setText(name);
		((TextView) activity.findViewById(R.id.act_show_more_info_text_category)).setText(categories);

		if( contacts != null && contacts != "" ) {

			VCard vcard = Ezvcard.parse(contacts).first();
			String separator = ", ";
			boolean allDisabled = true;
	
			if(vcard == null || vcard.getTelephoneNumbers() == null || vcard.getTelephoneNumbers().size()==0 || vcard.getTelephoneNumbers().get(0).getText() == "") {
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_tel)).setVisibility(View.GONE);
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_tel_input)).setVisibility(View.GONE);
			} else {
				allDisabled = false;
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_tel)).setVisibility(View.VISIBLE);
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_tel_input)).setVisibility(View.VISIBLE);
				StringBuilder sb = new StringBuilder();
				for(Telephone t : vcard.getTelephoneNumbers()) {
					sb.append(separator).append(t.getText());
				}
				String result = sb.substring(separator.length());
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_tel_input)).setText(result);
			}

			if(vcard == null || vcard.getEmails() == null || vcard.getEmails().size()==0 || vcard.getEmails().get(0).getValue() == "") {
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_mail)).setVisibility(View.GONE);
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_mail_input)).setVisibility(View.GONE);
			} else {
				allDisabled = false;
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_mail)).setVisibility(View.VISIBLE);
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_mail_input)).setVisibility(View.VISIBLE);
				StringBuilder sb = new StringBuilder();
				for(Email t : vcard.getEmails()) {
					sb.append(separator).append(t.getValue());
				}
				String result = sb.substring(separator.length());
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_mail_input)).setText(result);
			}

			if(vcard == null || vcard.getUrls() == null || vcard.getUrls().size()==0 ||  vcard.getUrls().get(0).getValue() == "") {
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_url)).setVisibility(View.GONE);
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_url_input)).setVisibility(View.GONE);
			} else {
				allDisabled = false;
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_url)).setVisibility(View.VISIBLE);
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_url_input)).setVisibility(View.VISIBLE);
				StringBuilder sb = new StringBuilder();
				for(Url t : vcard.getUrls()) {
					sb.append(separator).append(t.getValue());
				}
				String result = sb.substring(separator.length());
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_url_input)).setText(result);
			}

			if(vcard == null || vcard.getAddresses() == null || vcard.getAddresses().size()==0 || vcard.getAddresses().get(0).getExtendedAddress()== "") {
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_address)).setVisibility(View.GONE);
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_address_input)).setVisibility(View.GONE);
			} else {
				allDisabled = false;
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_address)).setVisibility(View.VISIBLE);
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_address_input)).setVisibility(View.VISIBLE);
				StringBuilder sb = new StringBuilder();
				for(Address t : vcard.getAddresses()) {
					sb.append(separator).append(t.getExtendedAddress());
				}
				String result = sb.substring(separator.length());
				((TextView) activity.findViewById(R.id.act_show_more_info_text_contactos_address_input)).setText(result);
			}
			if (allDisabled = true) {
				((LinearLayout) activity.findViewById(R.id.act_show_more_info_text_contacts)).setVisibility(View.GONE);
			}
			((LinearLayout) activity.findViewById(R.id.act_show_more_info_text_contacts)).setVisibility(View.VISIBLE);

		} else {
			((LinearLayout) activity.findViewById(R.id.act_show_more_info_text_contacts)).setVisibility(View.GONE);
		}

		if(pois.getTime() == null || pois.getTime().size() == 0) {
			((LinearLayout) activity.findViewById(R.id.act_show_more_info_text_sch)).setVisibility(View.GONE);
		} else {
			((LinearLayout) activity.findViewById(R.id.act_show_more_info_text_sch)).setVisibility(View.VISIBLE);

			for(POITermType poiTermType : pois.getTime()) {
				if(poiTermType.getType() == null || !poiTermType.getType().equalsIgnoreCase("text/icalendar")) {			
					((RelativeLayout) activity.findViewById(R.id.act_show_more_info_text_start_end)).setVisibility(View.GONE);
					((TextView) activity.findViewById(R.id.act_show_more_info_text_all)).setVisibility(View.VISIBLE);
					((TextView) activity.findViewById(R.id.act_show_more_info_text_all)).setText(poiTermType.getValue());
				} else if (poiTermType.getType().equalsIgnoreCase("text/icalendar")){	
					((RelativeLayout) activity.findViewById(R.id.act_show_more_info_text_start_end)).setVisibility(View.VISIBLE);
					((TextView) activity.findViewById(R.id.act_show_more_info_text_all)).setVisibility(View.GONE);

					StringReader sin = new StringReader(poiTermType.getValue());
					CalendarBuilder builder = new CalendarBuilder();
					try {
						Calendar calendar = builder.build(sin);


						for (Iterator i = calendar.getComponents().iterator(); i.hasNext();) {
							Component component = (Component) i.next();
							System.out.println("Component [" + component.getName() + "]");

							for (Iterator j = component.getProperties().iterator(); j.hasNext();) {
								Property property = (Property) j.next();
								System.out.println("Property [" + property.getName() + ", " + property.getValue() + "]");
							}
						}


						String start = calendar.getComponent("VEVENT").getProperty("DTSTART").getValue();
						String end = calendar.getComponent("VEVENT").getProperty("DTEND").getValue();											

						try {
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
							SimpleDateFormat output = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
							Date dstart = sdf.parse(start);
							Date dend = sdf.parse(end);
							String startDate = output.format(dstart);
							String endDate = output.format(dend);

							((TextView) activity.findViewById(R.id.act_show_more_info_text_start_input)).setText(startDate);
							((TextView) activity.findViewById(R.id.act_show_more_info_text_end_input)).setText(endDate);
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

		if(description == null || description == "") {
			((LinearLayout) activity.findViewById(R.id.act_show_more_info_text_desc)).setVisibility(View.GONE);
		} else {
			((LinearLayout) activity.findViewById(R.id.act_show_more_info_text_desc)).setVisibility(View.VISIBLE);
			((TextView) activity.findViewById(R.id.act_show_more_info_text_description)).setText(description);
		}

		final List<ImageContent> imageContent = DataReader.getImagesUri(pois);
		int pagerPosition = 0;

		if(imageContent != null && imageContent.size() != 0) {
			String[] arr = new String[imageContent.size()];  
			for(int i = 0; i< imageContent.size(); i++) {
				arr[i] = imageContent.get(i).getContent();
			}

			pager.setAdapter(new ImagePagerAdapter(arr));
			imageNumber.setText(positionGeral+"/"+imageContent.size());
		} else {
			imageNumber.setText(0+"/"+0);
			String imageUri = "drawable://" + R.drawable.ic_empty; 
			String[] arr = {imageUri};
			pager.setAdapter(new ImagePagerAdapter(arr));
		}

		pager.setOnPageChangeListener(
				new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						positionGeral = position+1;
						imageNumber.setText(positionGeral+"/"+imageContent.size());
					}
				});
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

			ImageLoader.getInstance().displayImage(images[position], imageView, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					spinner.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
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
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
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
