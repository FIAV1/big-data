package com.stormadvance.twitter_sentiment;

import static org.apache.storm.kafka.spout.FirstPollOffsetStrategy.EARLIEST;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.elasticsearch.bolt.EsIndexBolt;
import org.apache.storm.elasticsearch.common.DefaultEsTupleMapper;
import org.apache.storm.elasticsearch.common.EsConfig;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.kafka.spout.KafkaSpoutRetryExponentialBackoff;
import org.apache.storm.kafka.spout.KafkaSpoutRetryService;
import org.apache.storm.kafka.spout.KafkaSpoutRetryExponentialBackoff.TimeInterval;
import org.apache.storm.topology.TopologyBuilder;

public class TwitterTopology {
	public static final boolean debug = false;
	// Sentiment scores of few words are present in this file.
	// For more info on this, please check:
	// http://www2.imm.dtu.dk/pubdb/views/publication_details.php?id=6010
	private static String AFINN_SENTIMENT_FILE = null;
	// Name of the Topology. Used while launching the LocalCluster
	public static final String TOPOLOGY_NAME = "SentimentAnalysis";
	// Kafka
	public static String kafka = null;
	// Elasticsearch
	public static String elasticsearch = null;

	public static void main(String[] args) {
		if (debug) {
			kafka = "http://localhost:9094";
			elasticsearch = "http://localhost:9200";
			AFINN_SENTIMENT_FILE = "/home/federico/Work/big-data/docker_data/storm/data/AFINN-111.txt";
		} else if (args.length == 1) {
			kafka = "http://kafka:9094";
			elasticsearch = "http://elasticsearch:9200";
			AFINN_SENTIMENT_FILE = args[0];
		} else {
			throw new IllegalArgumentException("ERROR with AFINN file - debug: " + debug + "\targs: " + args.length);
		}

		// Now we create the topology
		TopologyBuilder builder = new TopologyBuilder();

		// Kafka spout config
		KafkaSpoutConfig<String, String> kafkaConfig = KafkaSpoutConfig.builder(kafka, "twitter")
				.setProp(ConsumerConfig.GROUP_ID_CONFIG, "twitter").setFirstPollOffsetStrategy(EARLIEST).build();
		// Set the kafka spout class
		builder.setSpout("KafkaSpout", new KafkaSpout<>(kafkaConfig), 1);

		// Parse json coming from Kafka
		builder.setBolt("json", new JSONParserBolt()).shuffleGrouping("KafkaSpout");

		// Sentiment coming from tweets
		builder.setBolt("sentiment", new SentimentBolt(AFINN_SENTIMENT_FILE)).shuffleGrouping("json");

		// Add content type header to avoid wrong header error
		Header[] headers = { new BasicHeader("Content-Type", "application/json") };
		// Set the Elasticsearch bolt
		builder
				.setBolt("PersistenceBolt",
						new EsIndexBolt(new EsConfig(elasticsearch).withDefaultHeaders(headers), new DefaultEsTupleMapper()), 1)
				.shuffleGrouping("sentiment");

		// Set Storm cluster configuration
		Config conf = new Config();
		conf.setNumWorkers(1);

		if (debug) {
			try {
				// Run Storm topology in local mode (DEBUG)
				LocalCluster cluster = new LocalCluster();
				cluster.submitTopology(TOPOLOGY_NAME, conf, builder.createTopology());
				// Wait for some time before exiting
				System.out.println("Waiting to consume from kafka");
				Thread.sleep(6000000);

				cluster.killTopology(TOPOLOGY_NAME);
				cluster.shutdown();
				cluster.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// Run Storm topology in the cluster (PRODUCTION)
			try {
				StormSubmitter.submitTopology(TOPOLOGY_NAME, conf, builder.createTopology());
			} catch (AlreadyAliveException | InvalidTopologyException | AuthorizationException e) {
				e.printStackTrace();
			}
		}
	}

	protected static KafkaSpoutRetryService getRetryService() {
		return new KafkaSpoutRetryExponentialBackoff(TimeInterval.microSeconds(500), TimeInterval.milliSeconds(2),
				Integer.MAX_VALUE, TimeInterval.seconds(10));
	}
}
