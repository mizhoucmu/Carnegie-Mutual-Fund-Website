package controller;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.util.Iterator;
import java.util.List;

import model.CommentDAO;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import databean.Comment;
 
public class SentimentAnalysis {
	static HttpURLConnection httpConn;
	private final String USER_AGENT = "Mozilla/5.0";
	String[] text = null;
	String start_date="2015-02-05";
	String end_date;
	// HTTP POST request
	public void sendPostRequest(CommentDAO commentDAO) throws Exception {
	
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey("dZvj3mjFbCkxdaPxyV1yYrwLN")
				.setOAuthConsumerSecret(
						"IefSAJMh3niXRIvHMHFioTdihcMOl020w4sNRamxJdUiqraokQ")
				.setOAuthAccessToken(
						"152178222-XOMzGNlS1UCIYUevjov0WsZvT2n036Kh82Qo9tjv")
				.setOAuthAccessTokenSecret(
						"Tz4GTDvC92QXv33dPwD6bDIGI1UDhjOOHUaLwi2IOF2dv");
		// search twitter for querying
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();

		try {
			
			Query query = new Query("#katy perry since:"+start_date+" until:2015-02-13");
			QueryResult result;
			List<Status> tweets;
			
			//do {
				result = twitter.search(query);
				tweets = result.getTweets();
				text=new String[tweets.size()];
				int i=0;
				for (Status tweet : tweets) {
					/*System.out.println("@" + tweet.getUser().getScreenName()
							+ " - " + tweet.getText());*/
					System.out.println("tweet:"+tweet.getText());
					text[i]=tweet.getText();
					i++;
				}
				System.out.println(text.length);
		//	} while ((query = result.nextQuery()) != null);
			
			
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to search tweets: " + te.getMessage());
			System.exit(-1);
		}
		
		
		
		
		
		
		
		
		
		
		//sentiment analysis
		String u = "http://www.sentiment140.com/api/bulkClassifyJson?appid=www.skandy@gmail.com";
		//Configure and open a connection to the site you will send the request
        URL url = new URL(u);
        try{
        		URLConnection urlConnection = url.openConnection();
					
					//by setting doOutput property to true we indicate that we will use this urlConnection to write data
					
					urlConnection.setDoOutput(true);
					
					// by setting content-type property to  application/x-www-form-urlencoded we define that
					// the data that we intent to write on the request's body consist of  key/value pairs
					
					urlConnection.setRequestProperty("content-type","application/x-www-form-urlencoded");
					
					//Get the request's output stream 
					OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
					JSONObject ob;
					JSONArray list = new JSONArray();
					for(int i=0;i<text.length;i++)
					{
						ob=new JSONObject();
						ob.put("text", text[i]);
						list.add(ob);
					}
					  StringWriter output = new StringWriter();
					  
					  list.writeJSONString(output);
					  
					  String jsonText = output.toString();
					  jsonText="{\"data\":"+jsonText+"}";
					  System.out.print(jsonText);
					  out.write(jsonText);
					out.flush();
					out.close();
					//Read server's response
					InputStream inputStream = urlConnection.getInputStream();
					String encoding = urlConnection.getContentEncoding();
					String body = IOUtils.toString(inputStream, encoding);
					
					JSONParser parser = new JSONParser();
					Object obj = parser.parse(body);
					JSONObject jsonObject = (JSONObject) obj;
					JSONArray msg = (JSONArray) jsonObject.get("data");
					System.out.println();
					int positive=0;
					int negative=0;
					int neutral=0;
					for(int i=0;i<msg.size();i++)
					{
						System.out.println("parsed:"+((JSONObject)msg.get(i)).get("polarity"));
						int polarity= Integer.parseInt(((JSONObject)msg.get(i)).get("polarity").toString());
						if(polarity==0)
						{
							negative++;
						}
						else if(polarity==2){
							neutral++;
						}
						else
							positive++;
					}
					
					
					//write to database
					Comment com=new Comment();
					com.setName("kp");
					com.setPositiveCon(positive);
					com.setNegativeCon(negative);
					com.setNeutralCon(neutral);
					commentDAO.create(com);
					
					
				} catch (IOException ex) {}
			}
}