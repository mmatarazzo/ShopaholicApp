package com.example.shopaholic;

import java.util.Date;
import java.util.Locale;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
	
	public final static String TEST_QUERY = "com.example.shopaholic.TEST_QUERY";
	public final static String CATEGORY_QUERY = "com.example.shopaholic.CATEGORY_QUERY";
	public final static String SEARCH_BAR_QUERY = "com.example.shopaholic.SEARCH_BAR_QUERY";
	public final static String SHARE_TEXT = "com.example.shopaholic.SHARE_TEXT";
	
	// ********************************* FOR TWITTER **************************************
	// ************************************************************************************
    /**
     * Register your here app https://dev.twitter.com/apps/new and get your
     * consumer key and secret
     * */
    static String TWITTER_CONSUMER_KEY = "tOZTqAJvJG99NPtx9iTtILEa5";
    static String TWITTER_CONSUMER_SECRET = "pL3JKr1BdBxuTzl48LbtNzgsVJ3LwWvgxPUj3xfDudQOAivppH";
    
    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    
    static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";
    
    // Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
    
    // Login button
    Button btnLoginTwitter;
    // Update status button
    Button btnUpdateStatus;
    // Logout button
    Button btnLogoutTwitter;
    // EditText for update
    EditText txtUpdate;
    // lbl update
    TextView lblUpdate;
    TextView lblUserName;
    
    // Not Logged in
    EditText txtNotLoggedIn;
    Button btnNotLoggedIn;
 
    // Progress dialog
    ProgressDialog pDialog;
 
    // Twitter
    private static Twitter twitter;
    private static RequestToken requestToken;
     
    // Shared Preferences
    private static SharedPreferences mSharedPreferences;
     
    // Internet Connection detector
    private ConnectionDetector cd;
     
    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();
    
    public static boolean immediatelyAfterLogin = false;
    public static boolean isLoggedIn = false;
    public static boolean tweeted = false;
    public static String tweet = "";
    // *************************************************************************************
    // *************************************************************************************
	
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
        
        // ******************* For Twitter ********************************
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        cd = new ConnectionDetector(getApplicationContext());
 
        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }
         
        // Check if twitter keys are set
        if(TWITTER_CONSUMER_KEY.trim().length() == 0 || TWITTER_CONSUMER_SECRET.trim().length() == 0){
            // Internet Connection is not present
            alert.showAlertDialog(MainActivity.this, "Twitter oAuth tokens", "Please set your twitter oauth tokens first!", false);
            // stop executing code by return
            return;
        }
        // *****************************************************************

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
    
    // *********************************** SEARCH FRAGMENT ***************************************
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
    		
    		
    		if (immediatelyAfterLogin) {
    			//showResults("((\"shirt\" AND \"deal\")^10 OR (\"shirt\" AND \"nationals\" AND \"deal\")^20 OR (\"nationals\" AND \"deal\")^10 OR (\"werth\" AND \"shirt\")^20)&mlt=true&mlt.fl=nationals");
    			showResults("((\"shirt\")^10 OR (\"shirt\" AND \"nationals\")^20 OR (\"nationals\")^10 OR (\"werth\" AND \"shirt\")^20)&sfield=geoloc&pt=38.9079485,-77.20635555&mlt=true&mlt.fl=nationals");
    			//immediatelyAfterLogin = false;
    		}
    		
    		
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
    // ************************************************************************************************
    
    // ************************************ MAPS FRAGMENT *********************************************
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
        	
        	//LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        	
        	/**
        	Criteria criteria = new Criteria();
        	criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);
            **/
        	
        	/**
        	 * LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        	 * LocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        	 */

        	//Location location = this.getMyLocation();               
        	//LatLng locationGPS = new LatLng(location.getLatitude(), location.getLongitude());
        	//mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationGPS, 16));
        	mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(38.867526,-77.087223), 14));
        	
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
    // **********************************************************************************
    
    // ******************************* FOR TWITTER **************************************
    /**
     * Function to login twitter
     * */
    private void loginToTwitter() {
        // Check if already logged in
        if (!isTwitterLoggedInAlready()) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
            Configuration configuration = builder.build();
             
            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();
 
            try {
                requestToken = twitter
                        .getOAuthRequestToken(TWITTER_CALLBACK_URL);
                this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse(requestToken.getAuthenticationURL())));
                immediatelyAfterLogin = true;
                isLoggedIn = true;
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {
            // user already logged into twitter
            Toast.makeText(getApplicationContext(),
                    "Already Logged into twitter", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Function to update status
     * */
    class updateTwitterStatus extends AsyncTask<String, String, String> {
 
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Updating to twitter...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
 
        /**
         * getting Places JSON
         * */
        protected String doInBackground(String... args) {
            Log.d("Tweet Text", "> " + args[0]);
            String status = args[0];
            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
                builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
                 
                // Access Token 
                String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
                // Access Token Secret
                String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");
                 
                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
                 
                // Update status
                twitter4j.Status response = twitter.updateStatus(status);
                 
                Log.d("Status", "> " + response.getText());
            } catch (TwitterException e) {
                // Error in updating status
                Log.d("Twitter Update Error", e.getMessage());
            }
            return null;
        }
 
        /**
         * After completing background task Dismiss the progress dialog and show
         * the data in UI Always use runOnUiThread(new Runnable()) to update UI
         * from background thread, otherwise you will get error
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Status tweeted successfully", Toast.LENGTH_SHORT)
                            .show();
                    // Clearing EditText field
                    txtUpdate.setText("");
                }
            });
        }
 
    }
    
    /**
     * Function to logout from twitter
     * It will just clear the application shared preferences
     * */
    private void logoutFromTwitter() {
        // Clear the shared preferences
        Editor e = mSharedPreferences.edit();
        e.remove(PREF_KEY_OAUTH_TOKEN);
        e.remove(PREF_KEY_OAUTH_SECRET);
        e.remove(PREF_KEY_TWITTER_LOGIN);
        e.commit();
 
        // After this take the appropriate action
        // I am showing the hiding/showing buttons again
        // You might not needed this code
        btnLogoutTwitter.setVisibility(View.GONE);
        btnUpdateStatus.setVisibility(View.GONE);
        txtUpdate.setVisibility(View.GONE);
        lblUpdate.setVisibility(View.GONE);
        lblUserName.setText("");
        lblUserName.setVisibility(View.GONE);
 
        btnLoginTwitter.setVisibility(View.VISIBLE);
        txtNotLoggedIn.setVisibility(View.VISIBLE);
        btnNotLoggedIn.setVisibility(View.VISIBLE);
        
        isLoggedIn = false;
    }
    
    /**
     * Check user already logged in your application using twitter Login flag is
     * fetched from Shared Preferences
     * */
    private boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }
 
    protected void onResume() {
        super.onResume();
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
    		
    		/**
    		if (isLoggedIn) {
    			lblUpdate.setVisibility(View.VISIBLE);
                txtUpdate.setVisibility(View.VISIBLE);
                btnUpdateStatus.setVisibility(View.VISIBLE);
                btnLogoutTwitter.setVisibility(View.VISIBLE);
    		}
    		**/
    		
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
    		
    		// ******************************* FOR TWITTER *************************************
    		// All UI elements
            btnLoginTwitter = (Button) rootView.findViewById(R.id.btnLoginTwitter);
            btnUpdateStatus = (Button) rootView.findViewById(R.id.btnUpdateStatus);
            btnLogoutTwitter = (Button) rootView.findViewById(R.id.btnLogoutTwitter);
            txtUpdate = (EditText) rootView.findViewById(R.id.txtUpdateStatus);
            lblUpdate = (TextView) rootView.findViewById(R.id.lblUpdate);
            lblUserName = (TextView) rootView.findViewById(R.id.lblUserName);
            
            txtNotLoggedIn = (EditText) rootView.findViewById(R.id.edit_share);
            btnNotLoggedIn = (Button) rootView.findViewById(R.id.share_button);
            
            // Shared Preferences
            mSharedPreferences = getApplicationContext().getSharedPreferences(
                    "MyPref", 0);
            
            /**
             * Twitter login button click event will call loginToTwitter() function
             * */
            btnLoginTwitter.setOnClickListener(new View.OnClickListener() {
     
                @Override
                public void onClick(View arg0) {
                    // Call login twitter function
                    loginToTwitter();
                }
            });
            
            /**
             * Button click event to Update Status, will call updateTwitterStatus()
             * function
             * */
            btnUpdateStatus.setOnClickListener(new View.OnClickListener() {
     
                @Override
                public void onClick(View v) {
                    // Call update status function
                    // Get the status from EditText
                    String status = txtUpdate.getText().toString();
                    
                    tweeted = true;
                    tweet = status;
     
                    // Check for blank text
                    if (status.trim().length() > 0) {
                        // update status
                        new updateTwitterStatus().execute(status);
                    } else {
                        // EditText is empty
                        Toast.makeText(getApplicationContext(),
                                "Please enter status message", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });
            
            /**
             * Button click event for logout from twitter
             * */
            btnLogoutTwitter.setOnClickListener(new View.OnClickListener() {
     
                @Override
                public void onClick(View arg0) {
                    // Call logout twitter function
                    logoutFromTwitter();
                }
            });
            
            /** This if conditions is tested once is
             * redirected from twitter page. Parse the uri to get oAuth
             * Verifier
             * */
            if (!isTwitterLoggedInAlready()) {
            	System.out.println("Passed");
                Uri uri = getIntent().getData();
                System.out.println(uri);
                if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
                    // oAuth verifier
                    String verifier = uri
                            .getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
                    
                    System.out.println("Passed 2");
     
                    try {
                        // Get the access token
                        AccessToken accessToken = twitter.getOAuthAccessToken(
                                requestToken, verifier);
     
                        System.out.println("Passed 3");
                        
                        // Shared Preferences
                        Editor e = mSharedPreferences.edit();
     
                        // After getting access token, access token secret
                        // store them in application preferences
                        e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
                        e.putString(PREF_KEY_OAUTH_SECRET,
                                accessToken.getTokenSecret());
                        // Store login status - true
                        e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
                        e.commit(); // save changes
     
                        Log.e("Twitter OAuth Token", "> " + accessToken.getToken());
     
                        // Hide login button
                        btnLoginTwitter.setVisibility(View.GONE);
                        txtNotLoggedIn.setVisibility(View.GONE);
                        btnNotLoggedIn.setVisibility(View.GONE);
     
                        // Show Update Twitter
                        lblUpdate.setVisibility(View.VISIBLE);
                        txtUpdate.setVisibility(View.VISIBLE);
                        btnUpdateStatus.setVisibility(View.VISIBLE);
                        btnLogoutTwitter.setVisibility(View.VISIBLE);
                         
                        // Getting user details from twitter
                        // For now i am getting his name only
                        long userID = accessToken.getUserId();
                        User user = twitter.showUser(userID);
                        String username = user.getName();
                        
                        long id = user.getId();
                        String screename = user.getScreenName();
                        String location = user.getLocation();
                        String description = user.getDescription();
                        String url = user.getURL();
                        int followers = user.getFollowersCount();
                        Status status = user.getStatus();
                        int friends = user.getFriendsCount();
                        Date created = user.getCreatedAt();
                        int favorites = user.getFavouritesCount();
                        String timezone = user.getTimeZone();
                        String lang = user.getLang();
                        boolean verified = user.isVerified();
                        int listed = user.getListedCount();
                         
                        // Displaying in xml ui
                        lblUserName.setText(Html.fromHtml("<b>Welcome " + username + "</b><br />" +
                        		"<b>ID: " + id +"</b><br />" +
                        		"<b>Location: " + location + "</b><br />" +
                        		"<b>Profile: " + description + "</b>"));
                    } catch (Exception e) {
                        // Check log for login errors
                        Log.e("Twitter Login Error", "> " + e.getMessage());
                    }
                }
            } else {
            	
            }
            // *******************************************************************************
    		
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
