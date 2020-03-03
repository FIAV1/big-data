package com.stormadvance.logprocessing;

import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonObject;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

/**
 * This class translate data in a form that is understandable by EsBolt
 */
public class EsTransformerBolt extends BaseBasicBolt {

  private static final long serialVersionUID = 1L;
  private JsonObject json = null;
  private String indexName = "logs";
  private String indexType = "apache";

  public EsTransformerBolt() {

  }

  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("id", "index", "type", "source"));
  }

  public void prepare(Map<String, Object> stormConf, TopologyContext context) {
    this.json = new JsonObject();
  }

  public void execute(Tuple input, BasicOutputCollector collector) {
    this.json.addProperty("ip", input.getStringByField("ip"));
    this.json.addProperty("dateTime", input.getStringByField("dateTime"));
    this.json.addProperty("request", input.getStringByField("request"));
    this.json.addProperty("response", input.getStringByField("response"));
    this.json.addProperty("bytesSent", input.getStringByField("bytesSent"));
    this.json.addProperty("referrer", input.getStringByField("referrer"));
    this.json.addProperty("useragent", input.getStringByField("useragent"));
    this.json.addProperty("location", input.getStringByField("location"));
    this.json.addProperty("country", input.getStringByField("country"));
    this.json.addProperty("browser", input.getStringByField("browser"));
    this.json.addProperty("os", input.getStringByField("os"));
    this.json.addProperty("keyword", input.getStringByField("keyword"));
    collector.emit(new Values(UUID.randomUUID().toString(), indexName, indexType, json.toString()));
  }
}