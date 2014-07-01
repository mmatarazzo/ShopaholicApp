package com.example.shopaholic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class ResultsActivity extends FragmentActivity implements ActionBar.TabListener {
	
	public final static String TIME_SERIES_VISUAL = "com.example.shopaholic.TIME_SERIES_VISUAL";
	public final static String TEST_QUERY = "com.example.shopaholic.TEST_QUERY";
	public final static String CATEGORY_QUERY = "com.example.shopaholic.CATEGORY_QUERY";
	public final static String SEARCH_BAR_QUERY = "com.example.shopaholic.SEARCH_BAR_QUERY";
	public final static String SHARE_TEXT = "com.example.shopaholic.SHARE_TEXT";
	 
    // This integer will store the XMLResourceParser events  
    private int event;  
    // Booleans that will tell if it's the tags we are looking for ??
    private boolean isCorrectTag = false;
    private boolean isLocTag = false;
    // We don't use namespaces
    private static final String ns = null;
    
    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";
    private static final String URL = "http://shopaholic.cs.vt.edu:8983/solr/select?q=";
    
    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = true; 
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = true; 
    public static String sPref = ANY;
    
    // Whether user entered a query or selected a category
    public static boolean searchBarQuery = false;
    public static boolean categoryQuery = false;
    
    // Globals for query and category selection
    public static String searchText = "";
    public static int category = 0;
    
    public static boolean isPlaces = false;
    
    // Global array list for textual results, associated venues, and all latlng coordinates
    ArrayList<String> results = new ArrayList<String>();
    ArrayList<String> venues = new ArrayList<String>();
    ArrayList<String> latlng = new ArrayList<String>();
    
    ArrayList<String> dates = new ArrayList<String>();
    
    // Some venues do not have latlng coordinates, and should not appear on map
    ArrayList<String> mapVenues = new ArrayList<String>();
    ArrayList<Boolean> hasResults = new ArrayList<Boolean>();
    
    //HashMap<String, Integer> counts = new HashMap<String, Integer>();
    
    // Store additional information relating text, # of deals, and # of locations to a venue
    ArrayList<String> encompasses = new ArrayList<String>();
    ArrayList<Integer> placeCounts = new ArrayList<Integer>();
    ArrayList<Integer> dealCounts = new ArrayList<Integer>();
    
    String userLat = "";
    String userLng = "";

    String shareMessage = "User Shared Deals";
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
		setContentView(R.layout.activity_results);

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
		
		// get Extras to retrieve query string
		Bundle extras = getIntent().getExtras();
		
		// Get the user's location
		//Location location = this.getMyLocation();               
		//userLat = String.valueOf(location.getLatitude());
		//userLng = String.valueOf(location.getLongitude());
				
		String placesUrl = "";
		
		boolean local = false;
		
		if (extras.containsKey(SEARCH_BAR_QUERY)) {
			searchBarQuery = true;
			searchText = extras.getString(SEARCH_BAR_QUERY);
		} else if (extras.getInt(CATEGORY_QUERY, 0) >= 0) {
			categoryQuery = true;
			category = extras.getInt(CATEGORY_QUERY);
		}
        
        // for local tweet processing---------------------------------------------
		/**
		if (searchBarQuery && local) {
        	try {
        		GetAttributesLocal(searchText);
        	} catch (XmlPullParserException e) {
        		//Display a toast message.
        		Toast.makeText(this, "Could not retrieve the current parser event", Toast.LENGTH_SHORT).show();
        	} catch (IOException e) {  
        		//Display a toast message.  
        		Toast.makeText(this, "Could not read XML file", Toast.LENGTH_SHORT).show();
        	}
        } else if (categoryQuery && local) {
        	//System.out.println("test");
        	
			switch (category) {
			case 0:
				searchText = "deals";
				break;
			case 1:
				searchText = "local";
				break;
			case 2:
				searchText = "retail";
				break;
			case 3:
				searchText = "entertainment";
				break;
			case 4:
				searchText = "dining";
				break;
			case 5:
				searchText = "services";
				break;				
			}
			
			try {
        		GetAttributesLocal(searchText);
        	} catch (XmlPullParserException e) {
        		//Display a toast message.
        		Toast.makeText(this, "Could not retrieve the current parser event", Toast.LENGTH_SHORT).show();
        	} catch (IOException e) {  
        		//Display a toast message.  
        		Toast.makeText(this, "Could not read XML file", Toast.LENGTH_SHORT).show();
        	}
        }
        **/
		
		// for server tweet processing---------------------------------------------
		if (!local) {
			if (categoryQuery) {
				switch (category) {
				case 0:
					searchText = "deal";
					break;
				case 1:
					searchText = "(local AND deal)^20 OR local^15";
					break;
				case 2:
					searchText = "(retail AND deal)^20 OR retail^15";
					break;
				case 3:
					searchText = "(entertainment AND deal)^20 OR entertainment^15";
					break;
				case 4:
					searchText = "(dining AND deal)^20 OR dining^15";
					break;
				case 5:
					searchText = "(service AND deal)^20 OR service^15";
					break;				
				}
			}
			try {
				//System.out.println(searchText);
				String fullURL = "";
				
				if (MainActivity.isLoggedIn) {
					searchText += "+nationals";
				}
				
				fullURL = URL + URLEncoder.encode(searchText, "UTF-8") + "&rows=30";
				
				//String fullURL = URL + URLEncoder.encode(searchText, "UTF-8") + "&rows=30";
				//System.out.println(fullURL);
				loadDeals(fullURL);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		isPlaces = true;
		// for Places API
		for (String v : venues) {
			System.out.println(v);
			//System.out.println(venues.size());
			//placesUrl = "https://maps.googleapis.com/maps/api/place/search/xml?keyword="+v+"&location="+userLat+","+userLng+"&radius=20000&sensor=false&key=AIzaSyDfioEwVJNylmvY06WQcD5ywSpu29phnTg";
			placesUrl = "https://maps.googleapis.com/maps/api/place/search/xml?keyword="+v+"&location=38.867526,-77.087223&radius=20000&sensor=false&key=AIzaSyDfioEwVJNylmvY06WQcD5ywSpu29phnTg";
			System.out.println(placesUrl);
			try {
				loadDeals(placesUrl);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		searchBarQuery = false;
	    categoryQuery = false;
		isPlaces = false;
		
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

	public void loadDeals(String url) throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		//System.out.println("check that we loaded: " + url);
		if ((sPref.equals(ANY)) && (wifiConnected || mobileConnected)) {
            //System.out.println("check 2");
			new DownloadXmlTask().execute(url).get();
        } else if ((sPref.equals(WIFI)) && (wifiConnected)) {
            new DownloadXmlTask().execute(url).get();
        } else {
            // show error
        }
	}
	
	private class DownloadXmlTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			// TODO Auto-generated method stub
			try {
				//System.out.println("How about now");
				return loadXmlFromNetwork(urls[0]);
			} catch (IOException e) {
				return getResources().getString(R.string.connection_error);
			} catch (XmlPullParserException e) {
				return getResources().getString(R.string.xml_error);
			}
		}
		
		@Override
	    protected void onPostExecute(String result) {     
			//ResultsActivity.this.onBackgroundTaskDataObtained(result);
	    }

		private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
			// TODO Auto-generated method stub
			InputStream stream = null;
		    // Instantiate the parser
		    SolrXmlParser solrXmlParser = new SolrXmlParser();
		    String deal = null;
		    try {
		    	//System.out.println("386 : " + urlString);
		        stream = downloadUrl(urlString);
		        /**
		        BufferedReader test = new BufferedReader(new InputStreamReader(stream));
		        String line;
		        while ((line = test.readLine()) != null) {
		        	System.out.println(line);
		        }
		        **/
		        deal = solrXmlParser.parse(stream);
		    // Makes sure that the InputStream is closed after the app is
		    // finished using it.
		    } finally {
		        if (stream != null) {
		            stream.close();
		        }
		    }
		    return deal;
		}

		// Given a string representation of a URL, sets up a connection and gets
		// an input stream.
		private InputStream downloadUrl(String urlString) throws IOException {
		    java.net.URL url = new java.net.URL (urlString);
		    System.out.println("394 : " + url);
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();	    
		    conn.setRequestMethod("GET");
		    //conn.setConnectTimeout(15000 /* milliseconds */);
		    conn.setReadTimeout(15000 /* milliseconds */);
		    //conn.setDoInput(true);
		    // Starts the query
		    conn.connect();
		    return conn.getInputStream();
		}
	}
	
	public class SolrXmlParser {
		
		public String parse(InputStream in) throws XmlPullParserException, IOException {
	        try {
	        	//System.out.println(in.toString());
	            XmlPullParser parser = Xml.newPullParser();
	            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	            parser.setInput(in, null);
	            parser.nextTag();
	            return GetAttributesServer(parser);
	        } finally {
	            in.close();
	        }
	        
	    }

		private String GetAttributesServer(XmlPullParser xpp) throws XmlPullParserException, IOException {
			// TODO Auto-generated method stub
			ArrayList<String> stores = new ArrayList<String>();
			stores.add("Wal-Mart");
			stores.add("Kroger");
			stores.add("Target");
			stores.add("Costco");
			stores.add("The Home Depot");
			stores.add("Walgreen");
			stores.add("CVS");
			stores.add("Lowe's");
			stores.add("Safeway");
			stores.add("McDonald's");
			stores.add("local");
			stores.add("retail");
			stores.add("entertainment");
			stores.add("dining");
			stores.add("services");
			stores.add("Nationals");
			stores.add("Wizards");
			stores.add("Hollister");
			stores.add("Abercrombie");
			stores.add("CarMax");
			//stores.add("Maternity");
			//stores.add("Nikko");
			
			ArrayList<String> initVenues = new ArrayList<String>();	
			//System.out.println("here?");
			
			int eventType = xpp.getEventType();
			StringBuffer stringBuffer = new StringBuffer();
			//StringBuffer encompass = new StringBuffer();
			String attr = "";
			//int dealCountVenue = 0;	
			int latlngcount = 0;
			
			results.add("Overall Sentiment: Neutral");
			
			if (MainActivity.tweeted) {
				//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				results.add("Deal: #shopaholicVT " + MainActivity.tweet + "\n\nSentiment: Negative\n\nTime: 2014-06-30T22:04:22Z\n\nTweeted by: Mike Matarazzo");
			}
			
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					//stringBuffer.append("--- Start XML ---");
			    } else if (eventType == XmlPullParser.START_TAG) {
			    	//stringBuffer.append("\nSTART_TAG: "+xpp.getName());
			    	if ((xpp.getName().equals("str") && xpp.getAttributeValue(ns, "name") != null) ||
			    			xpp.getName().equals("date")) {
			    		attr = xpp.getAttributeValue(ns, "name");
			    		//System.out.println(attr);
				    	if (attr.equals("full_tweet") || attr.equals("tweeted_by") || attr.equals("time") || 
				    			attr.equals("sentiment")) {
				    		isCorrectTag = true;
				    	}
			    	} else if ((xpp.getName().equals("lat") || xpp.getName().equals("lng"))) {
			    		isLocTag = true;
			    	}
			    } else if (eventType == XmlPullParser.END_TAG) {
			    	if (xpp.getName().equals("doc")) {
			    		String result = stringBuffer.toString();
						if (searchBarQuery && !isPlaces) {
							//System.out.println("check this here");
							//System.out.println(result);
							//System.out.println(searchText);
							//if (result.toLowerCase().contains(searchText.toLowerCase())) {
								results.add(result);
								//System.out.println("The result is " + result);
								for (String iv : initVenues) {
									if (result.contains(iv) && !venues.contains(iv)) venues.add(iv);
								}
							//}
						} else if (categoryQuery && !isPlaces) {
							//System.out.println("HERE?");
							results.add(result);
							for (String iv : initVenues) {
								if (result.contains(iv) && !venues.contains(iv)) venues.add(iv);
							}
							/**
							if (searchText == "all") {
								results.add(result);
								for (String iv : initVenues) venues.add(iv);
							} else if (searchText == "local") results.add(result);
							else if (searchText == "retail") results.add("RETAIL");
							else if (searchText == "entertainment") results.add("ENTERTAINMENT");
							else if (searchText == "dining") results.add("DINING");
							else if (searchText == "services") results.add("SERVICES");
							**/
						}
						stringBuffer = new StringBuffer();
			    	}
			    	//stringBuffer.append("\nEND_TAG: "+xpp.getName());
			    	isCorrectTag = false;
			    	isLocTag = false;
			    } else if (eventType == XmlPullParser.TEXT && isCorrectTag) {
			    	if (attr.equals("tweeted_by")) {
						if (!initVenues.contains(xpp.getText())) {
							//dealCounts.add(dealCountVenue);
							//dealCountVenue = 1;
							//counts.put(xpp.getText(), dealCount);
							//encompass.append("");
							//encompasses.add(encompass.toString());
							//encompass.setLength(0);
							//encompass.append(xpp.getText());
								
							//initVenues.add(xpp.getText());
							stringBuffer.append("\n\nTweeted by: " + xpp.getText());
						} else {
							//dealCountVenue++;
							//counts.put(xpp.getText(), dealCount);
							//encompass.append(xpp.getText());
							stringBuffer.append("\n\nTweeted by: " + xpp.getText());
						}
					} else if (attr.equals("full_tweet")) {
						//encompass.append(xpp.getText());
						stringBuffer.append("Deal: " + xpp.getText());
						
						// needs to go down to full tweet!
						for (String store : stores) {
							if (xpp.getText().toLowerCase().contains(store.toLowerCase())) initVenues.add(store);
						}
					} else if (attr.equals("time")) {
						//encompass.append("\n"+xpp.getText());
						stringBuffer.append("\n\nTime: " + xpp.getText());
						dates.add(xpp.getText());
					} else if (attr.equals("sentiment")) {
						if (xpp.getText().contains("Positive") || xpp.getText().contains("Positive Negative") ||
								xpp.getText().contains("Negative Positive") || xpp.getText().contains("Positive Neutral") ||
								xpp.getText().contains("Neutral Positive")) 
							stringBuffer.append("\n\nSentiment: Positive");
						else if (xpp.getText().contains("Neutral Negative") || xpp.getText().contains("Negative Neutral") ||
								xpp.getText().contains("Neutral"))
							stringBuffer.append("\n\nSentiment: Neutral");
						else if (xpp.getText().contains("Negative"))
							stringBuffer.append("\n\nSentiment: Negative");
					}
				} else if (eventType == XmlPullParser.TEXT && isLocTag) {
					latlng.add(xpp.getText());
					latlngcount++;
			    }
			    eventType = xpp.next();
			}
			//stringBuffer.append("\n--- End XML ---");
			
			System.out.println("LatLng count: " + latlngcount);
			
			if (isPlaces) {
				if (latlngcount == 0) hasResults.add(false);
				else {
					placeCounts.add(latlngcount);
					hasResults.add(true);
				}
			}
			
			/**
			String result = stringBuffer.toString();
			if (searchBarQuery && !isPlaces) {
				System.out.println("check this here");
				System.out.println(result);
				System.out.println(searchText);
				if (result.toLowerCase().contains(searchText.toLowerCase())) {
					results.add(result);
					System.out.println("The result is " + result);
					for (String iv : initVenues) {
						if (result.contains(iv)) venues.add(iv);
					}
				}
			} else if (categoryQuery && !isPlaces) {
				//System.out.println("HERE?");
				if (searchText == "all") {
					results.add(result);
					for (String iv : initVenues) venues.add(iv);
				} else if (searchText == "local") results.add(result);
				else if (searchText == "retail") results.add("RETAIL");
				else if (searchText == "entertainment") results.add("ENTERTAINMENT");
				else if (searchText == "dining") results.add("DINING");
				else if (searchText == "services") results.add("SERVICES");
			}
			**/
			
			return "";
		}
		
	}

	/**
	private void GetAttributesLocal(String string) throws XmlPullParserException, IOException {
		// TODO Auto-generated method stub
		
		// This object will store a reference to the Activity's resources  
	    Resources resources = this.getResources();
	    XmlResourceParser xpp = null;
	    
	    ArrayList<String> initVenues = new ArrayList<String>();
	    
	    int count = 8;
	    do {
	    	
			if (count == 8) xpp = resources.getXml(R.xml.officemaxdeal_403977248749154305);
	    	else if (count == 7) xpp = resources.getXml(R.xml.officemaxdeal_403992314923991040);
	    	else if (count == 6) xpp = resources.getXml(R.xml.dealsplus_374634322453479424);
	    	else if (count == 5) xpp = resources.getXml(R.xml.dealsplus_374641252672614400);
	    	else if (count == 4) xpp = resources.getXml(R.xml.oldnavy_403659964079951872);
	    	else if (count == 3) xpp = resources.getXml(R.xml.oldnavy_403936214748651520);
	    	else if (count == 2) xpp = resources.getXml(R.xml.tgifridays_403200516085129216);
	    	else if (count == 1) xpp = resources.getXml(R.xml.tgifridays_403669678876147713);
			xpp.next();
			
			int eventType = xpp.getEventType();
			StringBuffer stringBuffer = new StringBuffer();
			//StringBuffer encompass = new StringBuffer();
			String attr = "";
			//int dealCountVenue = 0;
			
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					//stringBuffer.append("--- Start XML ---");
				} else if (eventType == XmlPullParser.START_TAG) {
					//stringBuffer.append("\nSTART_TAG: "+xpp.getName());
					if (xpp.getName().equals("field")) {
						attr = xpp.getAttributeValue(ns, "name");
						if (attr.equals("full_tweet") || attr.equals("tweeted_by") || attr.equals("time")) {
							isCorrectTag = true;
						}
					}  	
				} else if (eventType == XmlPullParser.END_TAG) {
					//stringBuffer.append("\nEND_TAG: "+xpp.getName());
					isCorrectTag = false;
				} else if (eventType == XmlPullParser.TEXT && isCorrectTag) {
					
					if (attr.equals("tweeted_by")) {
						if (!initVenues.contains(xpp.getText())) {
							//dealCounts.add(dealCountVenue);
							//dealCountVenue = 1;
							//counts.put(xpp.getText(), dealCount);
							//encompass.append("");
							//encompasses.add(encompass.toString());
							//encompass.setLength(0);
							//encompass.append(xpp.getText());
							initVenues.add(xpp.getText());
							stringBuffer.append("\nTweeted by: " + xpp.getText());
						} else {
							//dealCountVenue++;
							//counts.put(xpp.getText(), dealCount);
							//encompass.append(xpp.getText());
							stringBuffer.append("\nTweeted by: " + xpp.getText());
						}
					} else if (attr.equals("full_tweet")) {
						//encompass.append(xpp.getText());
						stringBuffer.append("Deal: " + xpp.getText());
					} else if (attr.equals("time")) {
						//encompass.append("\n"+xpp.getText());
						stringBuffer.append("\nTime: " + xpp.getText());
						dates.add(xpp.getText());
					}
					
				}
				eventType = xpp.next();
			}
			//stringBuffer.append("\n--- End XML ---");
		
			String result = stringBuffer.toString();
			if (searchBarQuery) {
				if (result.contains(string)) {
					//System.out.println("This result has the query " + string + "!");
					results.add(result);
					for (String iv : initVenues) {
						if (result.contains(iv)) venues.add(iv);
					}
				}
			} else if (categoryQuery) {
				if (string == "all") {
					results.add(result);
					for (String iv : initVenues) venues.add(iv);
				} else if (string == "local") results.add(result);
				else if (string == "retail") results.add("RETAIL");
				else if (string == "entertainment") results.add("ENTERTAINMENT");
				else if (string == "dining") results.add("DINING");
				else if (string == "services") results.add("SERVICES");
			}
		
        
			// Release resources associated with the parser  
			xpp.close();
			count--;
        
	    } while (count > 0);
	}
	**/
	
	public void onBackgroundTaskDataObtained(String result) {
		// TODO Auto-generated method stub
		results.add(result);
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
		getMenuInflater().inflate(R.menu.results, menu);
		
		// Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void showVisualization(MenuItem mi) {
		Intent intentVisual = new Intent(this, VisualizationActivity.class);
		intentVisual.putExtra(TIME_SERIES_VISUAL, dates);
		startActivity(intentVisual);
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
        		return new VisualSectionFragment();
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
    	
    	@SuppressLint("NewApi")
		@Override
    	public View onCreateView(LayoutInflater inflater, ViewGroup container,
    			Bundle savedInstanceState) {
    		View rootView = inflater.inflate(R.layout.fragment_results_shop, container, false);
    		ListView listView = (ListView) rootView.findViewById(R.id.list);
    		
    		ArrayAdapter<String> adapterResults = new ArrayAdapter<String>(this.getActivity(),
					android.R.layout.simple_list_item_1, results);
			listView.setAdapter(adapterResults);
			
			if (MainActivity.immediatelyAfterLogin) {
				Toast.makeText(getApplicationContext(), "Welcome Mike! Some recommendations for you!", Toast.LENGTH_LONG).show();
				MainActivity.immediatelyAfterLogin = false;
			}
    		
    		return rootView;
    	}
    	
    }
	
	@SuppressLint("ValidFragment")
	public class VisualSectionFragment extends Fragment {
    	
    	public VisualSectionFragment() {
    	}
    	
    	private GoogleMap mMap;
    	
    	@Override
    	public View onCreateView(LayoutInflater inflater, ViewGroup container,
    			Bundle savedInstanceState) {
    		
    		View rootView = inflater.inflate(R.layout.fragment_results_visual, container, false);
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
        	Double latSum = 0.0;
        	Double lngSum = 0.0;   	
        	latSum += Double.parseDouble(userLat);
        	lngSum += Double.parseDouble(userLng);
        	**/
        	
        	for (int i = 0; i < venues.size(); i++) {
        		System.out.println(hasResults.get(i));
        		if (hasResults.get(i) == true) {
        			mapVenues.add(venues.get(i));
        		}
        	}
        	
        	int d = 0;
        	int p = 0;
        	int v = 0;
        			
        	for (int i = 0; i < latlng.size()-1; i+=2) {
        		mMap.addMarker(new MarkerOptions().position(
                		new LatLng(
                				Double.parseDouble(latlng.get(i)),
                				Double.parseDouble(latlng.get(i+1))
                				)).title(
                						//String.valueOf(dealCounts.get(d)) + 
                						"User tweet about " + mapVenues.get(v) ));
                						//"Negative sentiment for: Nationals shirts"));
        		
        		p+=2;
        		if (p >= placeCounts.get(v)) {
        			System.out.println(p);
        			p = 0;
        			v++;
        			d++;
        		}
        		
        		//mMap.addMarker(null).
        		
        		//latSum += Double.parseDouble(latlng.get(i));
        		//lngSum += Double.parseDouble(latlng.get(i+1));
        	}
        	             
        	//LatLng locationGPS = new LatLng(Double.parseDouble(userLat), Double.parseDouble(userLng));
        	//LatLng locationGPS = new LatLng(latSum/(latlng.size()/2), lngSum/(latlng.size()/2));
        	
        	//mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationGPS, 10));
        	mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(38.867526,-77.087223), 12));
        	
            /**
        	mMap.addMarker(new MarkerOptions().position(
            		new LatLng(38.897279, -77.189691)).title("Deal at Falls Church!"));
            mMap.addMarker(new MarkerOptions().position(
            		new LatLng(38.918618, -77.22302)).title("2 for 1 Samsung TVs at Best Buy until Saturday"));
            
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(38.9079485, -77.20635555), 12));
            **/
        }

    }
	
	@SuppressLint("ValidFragment")
	public class ShareSectionFragment extends Fragment {
    	
    	public ShareSectionFragment() {
    	}
    	
    	@Override
    	public View onCreateView(LayoutInflater inflater, ViewGroup container,
    			Bundle savedInstanceState) {
    		super.onCreateView(inflater, container, savedInstanceState);
            
    		// Get the query from the intent
    	    Intent intent = getIntent();
    	    if (intent.getExtras().containsKey(SHARE_TEXT)) {
    	    	shareMessage = intent.getStringExtra(MainActivity.SHARE_TEXT);
    	    }
    	    ArrayList<String> shares = new ArrayList<String>();
    	    shares.add(shareMessage);
    		
    		View rootView = inflater.inflate(R.layout.fragment_results_share, container, false);
    		
    		ListView listView = (ListView) rootView.findViewById(R.id.list_share);
    		
    		ArrayAdapter<String> adapterShare = new ArrayAdapter<String>(this.getActivity(),
					android.R.layout.simple_list_item_1, shares);
			listView.setAdapter(adapterShare);
    		/**
    		TextView textView = new TextView(this.getActivity());
    	    textView.setTextSize(20);
    	    textView.setText(message);
    	    **/
    	    
    	    //setContentView(textView);
            
    	    /**
            final Intent intentVisualButton = new Intent(this.getActivity(), VisualizationActivity.class);   
            View tsVisualButton = rootView.findViewById(R.id.ts_visual_button);
            tsVisualButton.setOnClickListener(
    				new OnClickListener() {
    					@Override
    					public void onClick(View view) {
    			    		intentVisualButton.putExtra(TIME_SERIES_VISUAL, "time_series");
    			    		startActivity(intentVisualButton);
    					}
    				}
    		);
    		**/
    		
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
			View rootView = inflater.inflate(R.layout.fragment_results_dummy,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

}
