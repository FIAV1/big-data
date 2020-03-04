package com.fiav1.log_processing;

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
  private final String indexName = "logs";
  private final String indexType = "apache";

  public EsTransformerBolt() {

  }

  public void declareOutputFields(final OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("id", "index", "type", "source"));
  }

  public void prepare(final Map<String, Object> stormConf, final TopologyContext context) {
    this.json = new JsonObject();
  }

  public void execute(final Tuple input, final BasicOutputCollector collector) {
    final JsonObject coord = new JsonObject();
    coord.addProperty("lat", input.getStringByField("latitude"));
    coord.addProperty("lon", input.getStringByField("longitude"));

    this.json.addProperty("request", input.getStringByField("request"));
    this.json.addProperty("bytes", input.getStringByField("bytes"));
    this.json.addProperty("auth", input.getStringByField("auth"));
    this.json.addProperty("ident", input.getStringByField("ident"));
    this.json.addProperty("clientip", input.getStringByField("clientip"));
    this.json.addProperty("referrer", input.getStringByField("referrer"));
    this.json.addProperty("httpversion", input.getStringByField("httpversion"));
    this.json.addProperty("host", input.getStringByField("host"));
    this.json.addProperty("verb", input.getStringByField("verb"));
    this.json.addProperty("response", input.getStringByField("response"));
    this.json.addProperty("agent", input.getStringByField("agent"));
    this.json.addProperty("agent", input.getStringByField("country_name"));
    this.json.add("coord", coord);
    this.json.addProperty("browser", input.getStringByField("browser"));
    this.json.addProperty("os", input.getStringByField("os"));
    this.json.addProperty("keyword", input.getStringByField("keyword"));
    this.json.addProperty("timestamp", input.getStringByField("timestamp"));

    collector.emit(new Values(UUID.randomUUID().toString(), indexName, indexType, json.toString()));
  }
}