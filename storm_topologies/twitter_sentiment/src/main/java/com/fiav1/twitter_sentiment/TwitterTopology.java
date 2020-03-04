package com.fiav1.twitter_sentiment;

import static org.apache.storm.kafka.spout.FirstPollOffsetStrategy.EARLIEST;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.elasticsearch.bolt.EsIndexBolt;
import org.apache.storm.elasticsearch.common.DefaultEsTupleMapper;
import org.apache.storm.elasticsearch.common.EsConfig;
import org.apache.storm.elasticsearch.common.EsTupleMapper;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.topology.TopologyBuilder;

public class TwitterTopology {
	// debug on/off
	public static final boolean debug = true;
	// Topology name
	public static final String TOPOLOGY_NAME = "SentimentAnalysis";
	// Kafka Host
	public static String KAFKA_HOST = debug ? "http://localhost:9094" : "http://kafka:9094";
	// Elasticsearch Host
	public static String ELASTICSEARCH_HOST = debug ? "http://localhost:9200" : "http://elasticsearch:9200";

	public static void main(final String[] args) throws Exception {
		// Storm Cluster config
		final Config conf = new Config();
		conf.setNumWorkers(1);

		// Kafka Spout config
		final KafkaSpoutConfig<String, String> kafkaConfig = KafkaSpoutConfig.builder(KAFKA_HOST, "twitter")
				.setProp(ConsumerConfig.GROUP_ID_CONFIG, "twitter").setFirstPollOffsetStrategy(EARLIEST).build();

		// Elasticsearch Bolt config
		final Header[] headers = { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
		final EsConfig esConfig = new EsConfig(ELASTICSEARCH_HOST).withDefaultHeaders(headers);
		final EsTupleMapper tupleMapper = new DefaultEsTupleMapper();

		// TOPOLOGY BUILDER
		final TopologyBuilder builder = new TopologyBuilder();

		// SPOUTS
		builder.setSpout("KafkaSpout", new KafkaSpout<>(kafkaConfig), 1);

		// BOLTS
		builder.setBolt("JsonParser", new JSONParserBolt()).shuffleGrouping("KafkaSpout");
		builder.setBolt("SentimentAnalyzer", new SentimentBolt()).shuffleGrouping("json");
		builder.setBolt("DataMemorizer", new EsIndexBolt(esConfig, tupleMapper), 1).shuffleGrouping("sentiment");

		if (debug) {
			// Run Storm topology in local mode (DEBUG)
			final LocalCluster cluster = new LocalCluster();
			cluster.submitTopology(TOPOLOGY_NAME, conf, builder.createTopology());

			Thread.sleep(6000000);
			cluster.killTopology(TOPOLOGY_NAME);
			cluster.shutdown();
			cluster.close();
		} else {
			// Run Storm topology in the cluster (PRODUCTION)
			StormSubmitter.submitTopology(TOPOLOGY_NAME, conf, builder.createTopology());
		}
	}
}
