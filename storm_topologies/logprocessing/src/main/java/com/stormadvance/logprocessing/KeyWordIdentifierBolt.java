package com.stormadvance.logprocessing;

import java.util.Map;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

/**
 * This class use the KeywordGenerator class to generate the search keyword from
 * referrer URL.
 * 
 */
public class KeyWordIdentifierBolt extends BaseBasicBolt {

	private static final long serialVersionUID = 1L;
	private KeywordGenerator keywordGenerator = null;

	public KeyWordIdentifierBolt() {

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("ip", "dateTime", "request", "response", "bytesSent", "referrer", "useragent",
				"location", "country", "browser", "os", "keyword"));
	}

	public void prepare(Map<String, Object> stormConf, TopologyContext context) {
		this.keywordGenerator = new KeywordGenerator();
	}

	public void execute(Tuple input, BasicOutputCollector collector) {
		String referrer = input.getStringByField("referrer").toString();
		// call the getKeyword(String referrer) method KeywordGenerator class to
		// generate the search keyword.
		Object keyword = keywordGenerator.getKeyword(referrer);
		// emits all the field emitted by previous bolt + keyword
		collector.emit(new Values(input.getStringByField("ip"), input.getStringByField("dateTime"),
				input.getStringByField("request"), input.getStringByField("response"), input.getStringByField("bytesSent"),
				input.getStringByField("referrer"), input.getStringByField("useragent"), input.getStringByField("location"),
				input.getStringByField("country"), input.getStringByField("browser"), input.getStringByField("os"), keyword));
	}
}