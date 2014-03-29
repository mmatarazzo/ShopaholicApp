package com.shopaholic.twitter.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.HashtagEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import twitter4j.internal.http.HttpResponseCode;

import com.shopaholic.twitter.utils.TwitterOAuthUtils;

public class CorpusBuild {
	private static Logger LOG = LoggerFactory.getLogger(CorpusBuild.class);
	private Twitter twitter = TwitterOAuthUtils.getTwitterInstance();
    private static final Date INIT_DATE = getInitDate();
    private static final Date FINAL_DATE = getFinalDate();
	private File corpusDir;
    private PrintWriter corpus;
    private PrintWriter corpusDeadUsers;
    private List<String> usernames;
    private static final int MAX_RETRIES = 5;
    private static final long SLEEP_TIME_MILLIS = 5000;
    private static final int PAGE_SIZE = 100;
    
    public CorpusBuild(File inputlist, File outputdir) throws IOException {
        usernames = new LinkedList<String>();
        String user;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(inputlist));
            while ((user = reader.readLine()) != null)
                usernames.add(user);
            corpusDir = outputdir;
        } finally {
            if (reader != null)
                reader.close();
        }
        if (LOG.isInfoEnabled())
            LOG.info("Read " + usernames.size() + " users from input file");
    }
    
    private static Date getInitDate() {
        Calendar c = Calendar.getInstance();
        c.set(2012, 8, 1, 0, 0, 0); // 1 August 2012 00:00:00
        return c.getTime();
    }

    private static Date getFinalDate() {
        Calendar c = Calendar.getInstance();
        c.set(2013, 11, 30, 23, 59, 59); // 30 November 2013 23:59:59
        return c.getTime();
    }
    
    public void run() throws IOException, InterruptedException {
    	int cursor = 0;

    	File fileForOmittedUserIds = new File("/Users/kruthika/Documents/MS/VT/CS5604_Information_Storage_and_Retrieval/Workspace_New/Shopaholic/resources", "omittedUsers.txt");
    	
  		 
        //boolean success = false;
    	//listSize = usernames.size();
        for (String username : usernames) {
        	 boolean success = false;
        	//int noOfTweets = 0;
            cursor++;
            if (LOG.isInfoEnabled())
                LOG.info("Building corpus of user (" + cursor +"): " + username);
        	 try {	 
        	 int startPage = findStartingPage(username);
        	 int currentTrial = 0;
             while (!success && currentTrial++ < MAX_RETRIES) {
               	 
                 if (LOG.isInfoEnabled())
                     LOG.info("Try (" + currentTrial + "/" + MAX_RETRIES + "): " + username);
                 success = crawl(username,startPage);
             }
//        	 ResponseList<Status> listOfTweets = twitter.getUserTimeline(username);
//        	 noOfTweets = noOfTweets + listOfTweets.size();
//        	 boolean done = false;
//        	 for (Status s1: listOfTweets) {
//        		// while (!done) {
//        		
//        		 newFile = new File(corpusDir, username+"_"+s1.getId());
//        		 corpus = new PrintWriter(new BufferedWriter(new FileWriter(newFile)));
//                 int currentTrial = 0;
//
//                 while (!success && currentTrial++ < MAX_RETRIES) {
//                	 
//                         if (LOG.isInfoEnabled())
//                             LOG.info("Try (" + currentTrial + "/" + MAX_RETRIES + "): " + username);
//                         
//	                         success = crawl(username,s1);
//	                         
//                 } 
//                 if (corpus != null) {
//                     corpus.close();
//                     if (LOG.isInfoEnabled())
//                         LOG.info("Closing corpus file: " + username+"_"+s1.getId());
//                 }
//                 
                 if (!success) {
                     if (LOG.isWarnEnabled())
                         LOG.warn("Could not crawl user: " + username);
                 } else if (LOG.isInfoEnabled())
                     LOG.info("Crawled corpus of user: " + username);
                 
//                // done = s.getCreatedAt().before(INIT_DATE);
//                // }
//                }
        	 } catch (TwitterException e) {
                 e.printStackTrace();
                 if (e.isCausedByNetworkIssue())
                     Thread.sleep(SLEEP_TIME_MILLIS);
                 else if (e.exceededRateLimitation()) {
                     int millisToSleep = 1000 * (e.getRetryAfter() + 10); // 10s slack
                     if (LOG.isInfoEnabled())
                         LOG.info("Sleeping for " + millisToSleep / 1000 + " seconds");
                     long before = System.currentTimeMillis();
                     try {
                         Thread.sleep(millisToSleep);
                     } catch (InterruptedException ie) {
                         ie.printStackTrace();
                     }
                     long now = System.currentTimeMillis();
                     if (LOG.isInfoEnabled())
                         LOG.info("Woke up! Slept for " + (now - before) / 1000 + " seconds");
                 } else if (e.resourceNotFound() || e.getStatusCode() == HttpResponseCode.FORBIDDEN
                         || e.getStatusCode() == HttpResponseCode.UNAUTHORIZED)
                     //break;
                	 corpusDeadUsers = new PrintWriter(new BufferedWriter(new FileWriter(fileForOmittedUserIds)));
               		 corpusDeadUsers.println(username+"\n");
               		 corpusDeadUsers.close();
                	 continue;
             } finally {
                 
                 
             }
        	 
        }
    	
    }


	private int findStartingPage(String username) throws TwitterException {
        Status status = null;
        int currentPage = 1, lastPage = 0;
        boolean found = false, close = false;
        
        if (LOG.isInfoEnabled())
            LOG.info("Doubling Phase: Start!");
        while (!found) {
            Paging paging = new Paging(currentPage * PAGE_SIZE, 1); // get only the first status instead of the full page
            ResponseList<Status> list; 
            //list = twitter.getUserTimeline(username, paging);
            if (username.matches("[0-9]+")) {
            	long longUserId = Long.parseLong(username);
            	list = twitter.getUserTimeline(longUserId, paging);
            } else {
            	list = twitter.getUserTimeline(username, paging);
            }
//            
            
            	
            if (LOG.isDebugEnabled())
                LOG.debug("Doubling Phase: got " + list.size() + " tweets");
            if (!list.isEmpty())
                status = list.iterator().next();
            found = list.isEmpty() || status.getCreatedAt().before(FINAL_DATE); // OR shortcut prevents NPE
            if (!found) {
                currentPage *= 2;
                if (LOG.isDebugEnabled())
                    LOG.debug("Doubling Phase: doubling, going to page " + currentPage);
            } else {
                if (LOG.isDebugEnabled())
                    LOG.debug("Doubling Phase: found end point! page " + currentPage);
                if (!list.isEmpty())
                    close = status.getCreatedAt().after(INIT_DATE);
                lastPage = currentPage / 2;
            }
        }

        if (LOG.isInfoEnabled())
            LOG.info("Closing Phase: Start!");
        found = currentPage <= 1;
        while (!found) {
            int currentGap = (currentPage - lastPage);
            close |= currentGap <= 1;
            if (LOG.isDebugEnabled())
                LOG.debug("CurrentGap=" + currentGap);
            if (close)
                currentPage--; // linear
            else
                currentPage -= currentGap / 2; // binary search
            if (LOG.isDebugEnabled())
                LOG.debug("Closing Phase: " + (close ? "linearly" : "binarily") + " reducing currentPage=" + currentPage);

            Paging paging = new Paging(currentPage * PAGE_SIZE, 1); // get only the first status instead of the full page
            ResponseList<Status> list = twitter.getUserTimeline(username, paging);
            if (LOG.isDebugEnabled())
                LOG.debug("Closing Phase: got " + list.size() + " tweets");
            if (!list.isEmpty()) {
                status = list.iterator().next();
                found = status.getCreatedAt().after(FINAL_DATE);
                close = status.getCreatedAt().after(INIT_DATE);
            }
            found |= currentPage <= 1;
        }
        if (found)
            if (LOG.isDebugEnabled())
                LOG.debug("Closing Phase: found end point! page " + currentPage);
        return Math.max(1, currentPage);
    }


	private boolean crawl(String username, int currentPage) throws TwitterException, IOException, InterruptedException {
		File newFile;
		boolean done = false;
		while (!done) {
		Paging paging = new Paging(currentPage++, PAGE_SIZE);
	 ResponseList<Status> listOfTweets;// = twitter.getUserTimeline(username,paging);
	 if (username.matches("[0-9]+")) {
     	long longUserId = Long.parseLong(username);
     	listOfTweets = twitter.getUserTimeline(longUserId, paging);
     } else {
    	 listOfTweets = twitter.getUserTimeline(username, paging);
     }
   	 //int noOfTweets = noOfTweets + listOfTweets.size();
   	 
   	 for (Status s: listOfTweets) {
   		 
   		
   		
   		 newFile = new File(corpusDir, username+"_"+s.getId()+".xml");
   		 corpus = new PrintWriter(new BufferedWriter(new FileWriter(newFile)));
       
                    if (LOG.isInfoEnabled())
                        LOG.info("Crawling Phase: Start!");
                        	corpus.println(formatFull(s));
                        
            if (corpus != null) {
                corpus.close();
                if (LOG.isInfoEnabled())
                    LOG.info("Closing corpus file: " + username+"_"+s.getId());
            }

            
            done = s.getCreatedAt().before(INIT_DATE);
            }
   	done |= listOfTweets.isEmpty();
    // list.getFeatureSpecificRateLimitStatus();
    Thread.sleep(SLEEP_TIME_MILLIS);
		
   	 }
        
        return true;
	}

	private String formatFull(Status s) {
		StringBuilder URLs = new StringBuilder();
    	StringBuilder userMentions = new StringBuilder();
    	StringBuilder hashTags = new StringBuilder();
    	String fullTweet = s.getText();
        
        //return s.getId() + sep + s.getUser().getScreenName() + sep + s.getCreatedAt() + sep + s.getText();
    	URLEntity[] urlMention = s.getURLEntities();
        for(URLEntity url : urlMention){
        	  URLs.append(url.getExpandedURL()+",");
        }
        if (URLs.length()!=0)
        	URLs.deleteCharAt(URLs.length()-1);
        
        UserMentionEntity[] userMention = s.getUserMentionEntities();
        for(UserMentionEntity user : userMention){
        	userMentions.append(user.getName()+",");
        	//User userr = (User) user;
        	//System.out.println(userr.getFollowersCount());
        	
        }
        if (userMentions.length()!=0)
        	userMentions.deleteCharAt(userMentions.length()-1);

        HashtagEntity[] hashTag = s.getHashtagEntities();
        for(HashtagEntity hashtag : hashTag){
        	hashTags.append(hashtag.getText()+",");
        }
        if (hashTags.length()!=0) {
        	hashTags.deleteCharAt(hashTags.length()-1);
        	System.out.println(hashTags);
        }

        if (fullTweet.contains("&")) {
        	fullTweet.replace("&", "&amp;");
        } 
        

        if (fullTweet.contains("<")) {
        	fullTweet.replace("<", "&lt;");
        } 
         
        if (fullTweet.contains(">")) {
        	fullTweet.replace(">", "&gt;");
        }
        
        
        StringBuilder statusToCorpus = new StringBuilder();
        statusToCorpus.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        statusToCorpus.append("<add>\n<doc>\n");
        statusToCorpus.append("<field name=\"full_tweet\">"+fullTweet+"</field>\n");
        statusToCorpus.append("<field name=\"tweet_id\">"+s.getId()+"</field>\n");
        statusToCorpus.append("<field name=\"tweeted_by\">"+s.getUser().getScreenName()+"</field>\n");
        statusToCorpus.append("<field name=\"time\">"+s.getCreatedAt()+"</field>\n");
        statusToCorpus.append("<field name=\"user_followers_count\">"+s.getUser().getFollowersCount()+"</field>\n");
        statusToCorpus.append("<field name=\"user_location\">"+s.getUser().getLocation()+"</field>\n");
        statusToCorpus.append("<field name=\"retweetcount\">"+s.getRetweetCount()+"</field>\n");
        statusToCorpus.append("<field name=\"is_favorite\">"+s.isFavorited()+"</field>\n");
        statusToCorpus.append("<field name=\"mentionedentities\">"+userMentions.toString()+"</field>\n");
        statusToCorpus.append("<field name=\"url\">"+URLs.toString()+"</field>\n");
        statusToCorpus.append("<field name=\"hashtags\">"+hashTags.toString()+"</field>\n");
        statusToCorpus.append("<field name=\"geolocation\">"+s.getGeoLocation()+"</field>\n");
        statusToCorpus.append("<field name=\"place\">"+s.getPlace()+"</field>\n");
        //statusToCorpus.append("<field name=\"tweeted_by\">"+s.getUser().getId()+"</field>\n");
//        
//        statusToCorpus.append("tweet_id:"+s.getId()+"\n");
//        statusToCorpus.append("tweeted_by:"+s.getUser().getScreenName()+"\n");
//        statusToCorpus.append("time:"+s.getCreatedAt()+"\n");
//        statusToCorpus.append("user_followers_count:"+s.getUser().getFollowersCount()+"\n");
//        statusToCorpus.append("user_location:"+s.getUser().getLocation()+"\n");
//        statusToCorpus.append("retweetcount:"+s.getRetweetCount()+"\n");
//        statusToCorpus.append("is_favorite:"+s.isFavorited()+"\n");
//        statusToCorpus.append("mentionedentities:"+userMentions.toString()+"\n");
//        statusToCorpus.append("url:"+URLs.toString()+"\n");
//        statusToCorpus.append("hashtags:"+hashTags.toString()+"\n");
//        statusToCorpus.append("geolocation:"+s.getGeoLocation()+"\n");
//        statusToCorpus.append("place:"+s.getPlace()+"\n");
        statusToCorpus.append("</doc>\n</add>");
        
        String status = statusToCorpus.toString();
        
        return status;
	}
	
	public static void main(String[] args) throws TwitterException, IOException, InterruptedException {
    	System.out.println("it is in");
        if (args.length < 2) {
            System.err.println("Usage: " + CorpusBuild.class.getName() + " <users_list_file> <output_directory>");
            System.exit(1);
        }
        new CorpusBuild(new File(args[0]), new File(args[1])).run();
    }

}