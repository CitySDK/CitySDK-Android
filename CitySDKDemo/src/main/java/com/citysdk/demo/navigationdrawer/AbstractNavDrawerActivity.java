package com.citysdk.demo.navigationdrawer;

import com.citysdk.demo.R;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public abstract class AbstractNavDrawerActivity extends FragmentActivity {

	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;

	private ListView mDrawerList;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;

	private NavDrawerActivityConfiguration navConf ;

	protected abstract NavDrawerActivityConfiguration getNavDrawerConfiguration();

	protected abstract void onNavItemSelected( int id, String label );
	
	protected abstract void performSearch();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		navConf = getNavDrawerConfiguration();

		setContentView(navConf.getMainLayout()); 

		mTitle = mDrawerTitle = getTitle();

		mDrawerLayout = (DrawerLayout) findViewById(navConf.getDrawerLayoutId());
		mDrawerList = (ListView) findViewById(navConf.getLeftDrawerId());
		mDrawerList.setAdapter(navConf.getBaseAdapter());
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mDrawerList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		this.initDrawerShadow();

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(
				this,
				mDrawerLayout,
				getDrawerIcon(),
				navConf.getDrawerOpenDesc(),
				navConf.getDrawerCloseDesc()
				) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				performSearch();
				//TourismAPI.getPlaceCategories((OnResultsListener)getFragmentManager().findFragmentById(MapsActivity().this) ,list);

				invalidateOptionsMenu();
				
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	protected void openDrawer() {
		mDrawerLayout.openDrawer(GravityCompat.START);
	}
	protected void closeDrawer() {
		mDrawerLayout.closeDrawer(GravityCompat.START);
	}
	
	protected void initDrawerShadow() {
		mDrawerLayout.setDrawerShadow(navConf.getDrawerShadow(), GravityCompat.START);
	}

	protected int getDrawerIcon() {
		return R.drawable.ic_drawer;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if ( navConf.getActionMenuItemsToHideWhenDrawerOpen() != null ) {
			boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
			for( int iItem : navConf.getActionMenuItemsToHideWhenDrawerOpen()) {
				menu.findItem(iItem).setVisible(!drawerOpen);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ( keyCode == KeyEvent.KEYCODE_MENU ) {
			if ( this.mDrawerLayout.isDrawerOpen(this.mDrawerList)) {
				this.mDrawerLayout.closeDrawer(this.mDrawerList);
			}
			else {
				this.mDrawerLayout.openDrawer(this.mDrawerList);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	protected DrawerLayout getDrawerLayout() {
		return mDrawerLayout;
	}

	protected ActionBarDrawerToggle getDrawerToggle() {
		return mDrawerToggle;
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	public void selectItem(int position) {
		NavDrawerItem selectedItem = navConf.getNavItems()[position];

		this.onNavItemSelected(selectedItem.getId(), selectedItem.getLabel());
		//mDrawerList.setItemChecked(position, true);

		if ( selectedItem.updateActionBarTitle()) {
			setTitle(selectedItem.getLabel());
		}

		if ( this.mDrawerLayout.isDrawerOpen(this.mDrawerList)) {
			// mDrawerLayout.closeDrawer(mDrawerList);
		}
	}

	public void selectItemMine(int id, String label) {

		NavDrawerItem[] list = navConf.getNavItems();
		for(int i = 0; i < list.length; i++) {
			if (list[i].getId() > id && list[i].getId() < id+100 ) {
				if (list[i].getLabel().equalsIgnoreCase(label)) {
					mDrawerList.setItemChecked(i, true);
				} else {
					mDrawerList.setItemChecked(i, false);
				}

			}	
		}
	}

	public void setItemCheckedCategories(int id, String label, boolean bool) {
		NavDrawerItem[] list = navConf.getNavItems();
		for(int i = 0; i < list.length; i++) {
			if (list[i].getId() > id && list[i].getId() < id+100 && list[i].getLabel().equalsIgnoreCase(label)) {
				mDrawerList.setItemChecked(i, bool);
			} 
		}	
	}

	public void setAdapterMine(NavDrawerItem[] menu) {
		mDrawerList.setAdapter(new NavDrawerAdapter(this, R.layout.act_navdrawer_item, menu ));
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}
}