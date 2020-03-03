package com.stormadvance.logprocessing;

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
	private String pathToGeoIP2City = null;

	public static void main(String args[]) {
		IpToCountryConverter ipToCountryConverter = new IpToCountryConverter(
				"/home/federico/Work/big-data/config/storm/data/GeoLite2-City.mmdb");
		String location = ipToCountryConverter.getLocation("192.168.64.1");
		String country = ipToCountryConverter.getCountryIsoCode("192.168.64.1");

		System.out.println("Location: " + location);
		System.out.println("Country: " + country);
	}

	public UserInformationGetterBolt(String pathToGeoIP2City) {
		// set the path of GeoLiteCity.dat file.
		this.pathToGeoIP2City = pathToGeoIP2City;
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("ip", "dateTime", "request", "response", "bytesSent", "referrer", "useragent",
				"location", "country", "browser", "os"));
	}

	public void prepare(Map<String, Object> stormConf, TopologyContext context) {
		this.ipToCountryConverter = new IpToCountryConverter(this.pathToGeoIP2City);
		this.userAgentTools = new UserAgentTools();
	}

	public void execute(Tuple input, BasicOutputCollector collector) {
		JsonObject json = JsonParser.parseString(input.getStringByField("value")).getAsJsonObject();

		String ip = json.get("clientip").getAsString();

		// calculate lat, lon, coutry iso code and city from ip
		String location = ipToCountryConverter.getLocation(ip);
		String country = ipToCountryConverter.getCountryIsoCode(ip);
		// calculate the browser from useragent.
		Object browser = userAgentTools.getBrowser(json.get("agent").getAsString())[1];
		// calculate the os from useragent.
		Object os = userAgentTools.getOS(json.get("agent").getAsString())[1];
		/*
		 * System.out.println("IP: " + json.get("clientip").getAsString());
		 * System.out.println("DATETIME: " + json.get("@timestamp").getAsString());
		 * System.out.println("REQUEST: " + json.has("request"));
		 * System.out.println("RESPONSE: " + json.get("response").getAsString());
		 * System.out.println("BYTESSENT: " + json.get("bytes").getAsString());
		 * System.out.println("REFERRER: " + json.get("referrer").getAsString());
		 * System.out.println("USERAGENT: " + json.get("agent").getAsString());
		 * System.out.println("LOCATION: " + location); System.out.println("COUNTRY: " +
		 * country); System.out.println("BROWSER: " + browser);
		 * System.out.println("OS: " + os);
		 */
		// emit new tuple
		collector.emit(new Values(json.get("clientip").getAsString(), json.get("@timestamp").getAsString(),
				json.has("request") ? json.get("request").getAsString() : "/", json.get("response").getAsString(),
				json.has("bytes") ? json.get("bytes").getAsString() : "0", json.get("referrer").getAsString(),
				json.get("agent").getAsString(), location, country, browser, os));
	}
}
