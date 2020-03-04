package com.fiav1.twitter_sentiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import com.google.common.base.Splitter;
import com.google.gson.JsonObject;

/**
 * Breaks each tweet into words and calculates the sentiment of each tweet and
 * assocaites the sentiment value to the State and logs the same to the console
 * and also logs to the file.
 */
public class SentimentBolt extends BaseBasicBolt {
	private static final long serialVersionUID = 1L;
	private String indexName = "twitter";
	private String typeName = "sentiment";

	public SentimentBolt() {
	}

	private Map<String, Integer> afinnSentimentMap = new HashMap<String, Integer>();

	public void prepare(Map<String, Object> map, TopologyContext topologyContext) {
		// Bolt reads the AFINN Sentiment file and stores the key,value pairs to a Map.
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(getClass().getClassLoader().getResource("AFINN-111.txt").getFile()));
			String line;
			while ((line = br.readLine()) != null) {
				String[] tabSplit = line.split("\t");
				afinnSentimentMap.put(tabSplit[0], Integer.parseInt(tabSplit[1]));
			}
			br.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
			System.exit(1);
		}

	}

	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
		outputFieldsDeclarer.declare(new Fields("source", "index", "type", "id"));
	}

	public void execute(Tuple input, BasicOutputCollector collector) {
		String tweet = input.getStringByField("tweet_full_text");
		int sentimentCurrentTweet = getSentimentOfTweet(tweet);
		JsonObject json = new JsonObject();
		json.addProperty("user_name", input.getStringByField("user_name"));
		json.addProperty("user_screen_name", tweet);
		json.addProperty("user_location", input.getStringByField("user_location"));
		json.addProperty("tweet_created_at", input.getStringByField("tweet_created_at"));
		json.addProperty("sentiment", sentimentCurrentTweet);
		collector.emit(new Values(json.toString(), indexName, typeName, UUID.randomUUID().toString()));
	}

	/**
	 * Gets the sentiment of the current tweet.
	 *
	 * @param status -- Status Object.
	 * @return sentiment of the current tweet.
	 */
	private int getSentimentOfTweet(String text) {
		// Remove all punctuation and new line chars in the tweet.
		String tweet = text.replaceAll("\\p{Punct}|\\n", " ").toLowerCase();
		// Splitting the tweet on empty space.
		Iterable<String> words = Splitter.on(' ').trimResults().omitEmptyStrings().split(tweet);
		int sentimentOfCurrentTweet = 0;
		// Loop thru all the words and find the sentiment of this tweet.
		for (String word : words) {
			if (afinnSentimentMap.containsKey(word)) {
				sentimentOfCurrentTweet += afinnSentimentMap.get(word);
			}
		}
		return sentimentOfCurrentTweet;
	}
}
