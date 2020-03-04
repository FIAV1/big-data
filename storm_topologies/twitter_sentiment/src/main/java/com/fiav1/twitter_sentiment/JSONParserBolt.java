package com.fiav1.twitter_sentiment;

import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class JSONParserBolt extends BaseBasicBolt {
	private static final long serialVersionUID = 1L;

	public void prepare(Map<String, Object> stormConf, TopologyContext context) {
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer
				.declare(new Fields("user_name", "user_screen_name", "user_location", "tweet_full_text", "tweet_created_at"));
	}

	public void execute(Tuple input, BasicOutputCollector collector) {
		JsonObject json = JsonParser.parseString(input.getStringByField("value")).getAsJsonObject();
		collector.emit(new Values(json.get("user_name").getAsString(), json.get("user_screen_name").getAsString(),
				json.get("user_location").getAsString(), json.get("tweet_full_text").getAsString(),
				json.get("tweet_created_at").getAsString()));
	}
}
