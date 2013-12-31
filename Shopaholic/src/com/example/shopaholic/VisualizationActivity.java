package com.example.shopaholic;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import com.androidplot.xy.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class VisualizationActivity extends Activity {
	
	public final static String TIME_SERIES_VISUAL = "com.example.shopaholic.TIME_SERIES_VISUAL";
	private XYPlot plot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the message from the intent
	    Bundle extras = getIntent().getExtras();
	    ArrayList<String> tweetDates = extras.getStringArrayList(TIME_SERIES_VISUAL);
	    
	    DateFormat original = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
	    DateFormat modified = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    DateFormat newFormat = new SimpleDateFormat("MM-dd-yyyy");
	    ArrayList<String> converted = new ArrayList<String>();
	    ArrayList<Long> epoch = new ArrayList<Long>();
	    
	    for (String td : tweetDates) {
	    	try {
	    		String convert = newFormat.format(modified.parse(td));
	    		//System.out.println(convert);
				epoch.add((newFormat.parse(convert).getTime())/1000);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    Collections.sort(epoch);
	    ArrayList<Long> ordered = epoch;
	    
	    ArrayList<Long> datesu = new ArrayList<Long>();
	    ArrayList<Integer> counts = new ArrayList<Integer>();
	    //for (Long m : epoch) System.out.println(m);
	    
	    HashMap<Long, Integer> dateCounts = new HashMap<Long, Integer>();
	    
	    for (Long m : ordered) {
	    	System.out.println(m);
	    	if (datesu.contains(m)) {
	    		int count = counts.get(counts.size()-1);
	    		count++;
	    		counts.remove(counts.size()-1);
	    		counts.add(count);
	    	} else {
	    		datesu.add(m);
	    		counts.add(1);
	    	}
	    	/**
	    	if (dateCounts.containsKey(m)) {
	    		Integer i = dateCounts.get(m);
	    		i++;
	    		dateCounts.put(m, i);
	    		} else dateCounts.put(m, new Integer(1));
	    		**/
	    }
		
		setContentView(R.layout.activity_visualization);
		// Show the Up button in the action bar.
		//setupActionBar();
		
		// initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.time_series);
        
        Number[] numDeals = {5, 8, 9, 3, 4, 7
        		//, 6
        		};
        
        // an array of years in milliseconds:
        Number[] dates = {
        		/**
                978307200,  // 2001
                1009843200, // 2002
                1041379200, // 2003
                1072915200, // 2004
                1104537600  // 2005
                **/
                
        		1384128000, // Nov-11
        		1384214400,	// Nov-12
        		1384300800,	// ...
        		1384387200,
        		1384473600,
        		1384560000
        		//,1384646400	// Nov-17
        };
        
        // create our series from our array of nums:
        /**
        XYSeries series2 = new SimpleXYSeries(
                Arrays.asList(dates),
                Arrays.asList(numDeals),
                "Deals/Day");
                **/
        
        ArrayList<Number> finalDates = new ArrayList<Number>();
        ArrayList<Number> finalCounts = new ArrayList<Number>();
        
        /**
        for (Long dc : dateCounts.keySet()) {
        	finalDates.add(dc);
        	System.out.println(dc);
        }
        for (Integer dc : dateCounts.values()) {
        	finalCounts.add(dc);
        	//System.out.println(dc);
        }
        **/
        
        XYSeries series2 = new SimpleXYSeries(datesu, counts, "Deals/Date 2013");
        
        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        plot.getGraphWidget().getDomainGridLinePaint().setColor(Color.BLACK);
        plot.getGraphWidget().getDomainGridLinePaint().
                setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        plot.getGraphWidget().getRangeGridLinePaint().setColor(Color.BLACK);
        plot.getGraphWidget().getRangeGridLinePaint().
                setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
        
        // Create a formatter to use for drawing a series using LineAndPointRenderer:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(
                Color.rgb(0, 100, 0),                   // line color
                Color.rgb(0, 100, 0),                   // point color
                Color.rgb(100, 200, 0), null);                // fill color
        
        // setup our line fill paint to be a slightly transparent gradient:
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        
        // ugly usage of LinearGradient. unfortunately there's no way to determine the actual size of
        // a View from within onCreate.  one alternative is to specify a dimension in resources
        // and use that accordingly.  at least then the values can be customized for the device type and orientation.
        lineFill.setShader(new LinearGradient(0, 0, 200, 200, Color.WHITE, Color.GREEN, Shader.TileMode.CLAMP));
        
        LineAndPointFormatter formatter  =
                new LineAndPointFormatter(Color.rgb(0,0,0), Color.BLUE, Color.RED, null);
        formatter.setFillPaint(lineFill);
        plot.getGraphWidget().setPaddingRight(2);
        plot.addSeries(series2, formatter);
        
        // draw a domain tick for each year:
        plot.setDomainStep(XYStepMode.SUBDIVIDE, 13);
        plot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);
        //plot.setTicksPerRangeLabel(11);
        plot.setDomainBoundaries(1357016400, 1388552400, BoundaryMode.FIXED);
        plot.setRangeBoundaries(0, 10, BoundaryMode.FIXED);

        // customize our domain/range labels
        plot.setDomainLabel("Date");
        plot.setRangeLabel("# of Deals");

        // get rid of decimal points in our range labels:
        plot.setRangeValueFormat(new DecimalFormat("0"));

        plot.setDomainValueFormat(new Format() {

            // create a simple date format that draws on the year portion of our timestamp.
            // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
            // for a full description of SimpleDateFormat.
            private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM");

            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

                // because our timestamps are in seconds and SimpleDateFormat expects milliseconds
                // we multiply our timestamp by 1000:
                long timestamp = ((Number) obj).longValue() * 1000;
                Date date = new Date(timestamp);
                return dateFormat.format(date, toAppendTo, pos);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;

            }
        });
        
        /**
        // Create a couple arrays of y-values to plot:
        Number[] series1Numbers = {1, 8, 5, 2, 7, 4};
        Number[] series2Numbers = {4, 6, 3, 8, 2, 10};
 
        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Series1");                             // Set the display title of the series
 
        // same as above
        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");
 
        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        /**
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_plf1);
                **/
        /**
        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.RED, Color.GREEN, Color.BLUE, null);
 
        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);
 
        // same as above:
        /**
        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_plf2);
                **/
        /**
        LineAndPointFormatter series2Format = new LineAndPointFormatter(Color.MAGENTA, Color.YELLOW, Color.WHITE, null);
        plot.addSeries(series2, series2Format);
 
        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);
        **/
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.visualization, menu);
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

}
