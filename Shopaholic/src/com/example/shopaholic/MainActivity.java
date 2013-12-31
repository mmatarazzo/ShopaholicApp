package com.example.shopaholic;

import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
	
	public final static String TEST_QUERY = "com.example.shopaholic.TEST_QUERY";
	public final static String CATEGORY_QUERY = "com.example.shopaholic.CATEGORY_QUERY";
	public final static String SEARCH_BAR_QUERY = "com.example.shopaholic.SEARCH_BAR_QUERY";
	public final static String SHARE_TEXT = "com.example.shopaholic.SHARE_TEXT";
	
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
            		.setText(mSectionsPagerAdapter.getPageTitle(i))
            		.setTabListener(this));
        }
        
        handleIntent(getIntent());
    }
    
    public void onNewIntent(Intent intent) {
    	setIntent(intent);
    	handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
		// TODO Auto-generated method stub
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// handles a search query
			String query = intent.getStringExtra(SearchManager.QUERY);
			showResults(query);
		}
	}
    
    private void showResults(String query) {
    	Intent queryIntent = new Intent(getApplicationContext(), ResultsActivity.class);
		queryIntent.putExtra(SEARCH_BAR_QUERY, query);
		startActivity(queryIntent);
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        
        return true;
    }
    
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	switch (position) {
        	case 0:
        		return new ShopSectionFragment();
        	case 1:
        		return new MapsSectionFragment();
        	case 2:
        		return new ShareSectionFragment();
        	default:
        		// getItem is called to instantiate the fragment for the given page.
                // Return a DummySectionFragment (defined as a static inner class
                // below) with the page number as its lone argument.
                Fragment fragment = new DummySectionFragment();
                Bundle args = new Bundle();
                args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
        	}
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }
    
    @SuppressLint("ValidFragment")
	public class ShopSectionFragment extends Fragment {
    	
    	public ShopSectionFragment() {
    	}
    	
    	@Override
    	public View onCreateView(LayoutInflater inflater, ViewGroup container,
    			Bundle savedInstanceState) {
    		View rootView = inflater.inflate(R.layout.fragment_section_shop, container, false);
    		
    		String[] listViewList = new String[] {"All Deals", "Deals Near Me", "Retail", 
    				"Entertainment", "Dining", "Services"};		
    		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), 
    				android.R.layout.simple_list_item_1, listViewList);
    		
    		ListView listView = (ListView) rootView.findViewById(R.id.list);
    		listView.setAdapter(adapter);
    		
    		// Define the on-click listener for the list items
            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Build the Intent used to open WordActivity with a specific word Uri
                    Intent textResultsIntent = new Intent(getApplicationContext(), ResultsActivity.class);
                    
                    textResultsIntent.putExtra(CATEGORY_QUERY, position);
		    		startActivity(textResultsIntent);
                }
            });
    		
    		return rootView;
    	}
    }
    
    @SuppressLint("ValidFragment")
    public class MapsSectionFragment extends Fragment implements LocationListener {
    	
    	public MapsSectionFragment() {
    	}
    	
    	private GoogleMap mMap;
    	
    	@Override
    	public View onCreateView(LayoutInflater inflater, ViewGroup container,
    			Bundle savedInstanceState) {
    		View rootView = inflater.inflate(R.layout.fragment_section_maps, container, false);
    		
    		setUpMapIfNeeded();
    		
    		return rootView;
    	}

        /**
         * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
         * installed) and the map has not already been instantiated.. This will ensure that we only ever
         * call {@link #setUpMap()} once when {@link #mMap} is not null.
         * <p>
         * If it isn't installed {@link SupportMapFragment} (and
         * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
         * install/update the Google Play services APK on their device.
         * <p>
         * A user can return to this FragmentActivity after following the prompt and correctly
         * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
         * completely destroyed during this process (it is likely that it would only be stopped or
         * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
         * {@link #onResume()} to guarantee that it will be called.
         */
        private void setUpMapIfNeeded() {
            // Do a null check to confirm that we have not already instantiated the map.
            if (mMap == null) {
                // Try to obtain the map from the SupportMapFragment.
                mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                        .getMap();
                // Check if we were successful in obtaining the map.
                if (mMap != null) {
                    setUpMap();
                }
            }
        }

        /**
         * This is where we can add markers or lines, add listeners or move the camera.
         * 
         * This should only be called once and when we are sure that {@link #mMap} is not null.
         */
        private void setUpMap() {
        	mMap.setMyLocationEnabled(true);
        	
        	/**
        	 * LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        	 * LocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        	 */

        	Location location = this.getMyLocation();               
        	LatLng locationGPS = new LatLng(location.getLatitude(), location.getLongitude());
        	mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationGPS, 16));
        	
        }
        
        // from http://stackoverflow.com/questions/18132294/cant-find-user-location-google-maps-api-v2
        private Location getMyLocation() {
    	    // Get location from GPS if it's available
    	    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    	    Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

    	    // Location wasn't found, check the next most accurate place for the current location
    	    if (myLocation == null) {
    	        Criteria criteria = new Criteria();
    	        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
    	        // Finds a provider that matches the criteria
    	        String provider = lm.getBestProvider(criteria, true);
    	        // Use the provider to get the last known location
    	        myLocation = lm.getLastKnownLocation(provider);
    	    }
    	    return myLocation;
    	}

		@Override
		public void onLocationChanged(Location arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		} 	
    }
    
    @SuppressLint("ValidFragment")
	public class ShareSectionFragment extends Fragment {
    	
    	public ShareSectionFragment() {
    	}
    	
    	@Override
    	public View onCreateView(LayoutInflater inflater, ViewGroup container,
    			Bundle savedInstanceState) {
    		View rootView = inflater.inflate(R.layout.fragment_section_share, container, false);
    		
    		final Intent intent = new Intent(this.getActivity(), ResultsActivity.class);
    		
    		View shareButton = rootView.findViewById(R.id.share_button);
    		shareButton.setOnClickListener(
    				new OnClickListener() {
    					@Override
    					public void onClick(View view) {
    						//final Intent intent = new Intent(this.getActivity(), ResultsActivity.class);
    			    		EditText editText = (EditText) findViewById(R.id.edit_share);
    			    		String message = editText.getText().toString();
    			    		intent.putExtra(SHARE_TEXT, message);
    			    		startActivity(intent);
    					}
    				}
    		);
    		
    		return rootView;
    	}
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

}
