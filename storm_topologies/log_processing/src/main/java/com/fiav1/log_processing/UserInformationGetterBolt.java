package com.fiav1.log_processing;

import java.sql.Timestamp;
import java.util.Date;
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

/**
 * This class use the IpToCountryConverter and UserAgentTools class to calculate
 * the country, os and browser from log line.
 * 
 */
public class UserInformationGetterBolt extends BaseBasicBolt {

	private static final long serialVersionUID = 1L;
	private IpToCountryConverter ipToCountryConverter = null;
	private UserAgentTools userAgentTools = null;

	public static void main(String args[]) {
		IpToCountryConverter ipToCountryConverter = new IpToCountryConverter();
		Map<String, String> location = ipToCountryConverter.getLocation("10.8.64.20");
		String latitude = location.get("latitude");
		String longitude = location.get("longitude");

		System.out.println("Location: " + latitude + ", " + longitude);
	}

	public UserInformationGetterBolt() {
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("request", "bytes", "auth", "ident", "clientip", "referrer", "httpversion", "host",
				"verb", "response", "agent", "country_name", "latitude", "longitude", "browser", "os", "timestamp"));
	}

	public void prepare(Map<String, Object> stormConf, TopologyContext context) {
		this.ipToCountryConverter = new IpToCountryConverter();
		this.userAgentTools = new UserAgentTools();
	}

	public void execute(Tuple input, BasicOutputCollector collector) {
		JsonObject json = JsonParser.parseString(input.getStringByField("value")).getAsJsonObject();

		String ip = json.get("clientip").getAsString();

		// calculate lat, lon and coutry iso code from ip
		Map<String, String> location = ipToCountryConverter.getLocation(ip);
		String latitude = location.get("latitude");
		String longitude = location.get("longitude");
		String countryName = ipToCountryConverter.getCountryName(ip);
		// calculate the browser from useragent.
		Object browser = userAgentTools.getBrowser(json.get("agent").getAsString())[1];
		// calculate the os from useragent.
		Object os = userAgentTools.getOS(json.get("agent").getAsString())[1];
		// emit new tuple
		collector.emit(new Values(json.has("request") ? json.get("request").getAsString() : "/",
				json.has("bytes") ? json.get("bytes").getAsString() : "0",
				json.has("auth") ? json.get("auth").getAsString() : "N/A",
				json.has("ident") ? json.get("ident").getAsString() : "N/A",
				json.has("clientip") ? json.get("clientip").getAsString() : "N/A",
				json.has("referrer") ? json.get("referrer").getAsString() : "N/A",
				json.has("httpversion") ? json.get("httpversion").getAsString() : "N/A",
				json.has("host") ? json.get("host").getAsString() : "N/A",
				json.has("verb") ? json.get("verb").getAsString() : "N/A",
				json.has("response") ? json.get("response").getAsString() : "N/A",
				json.has("agent") ? json.get("agent").getAsString() : "N/A",
				json.has("@timestamp") ? json.get("@timestamp").getAsString() : new Timestamp(new Date().getTime()).toString(),
				countryName, latitude, longitude, browser, os));
	}
}
