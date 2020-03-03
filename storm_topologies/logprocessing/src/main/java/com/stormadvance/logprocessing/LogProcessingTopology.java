package com.stormadvance.logprocessing;

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

public class LogProcessingTopology {
	private static final boolean debug = true;
	private static String pathToGeoIP2City = null;
	private static final String topologyName = "apache_log_topology";

	public static void main(String[] args) throws Exception {
		if (debug) {
			pathToGeoIP2City = "/home/federico/Work/big-data/config/storm/data/GeoLite2-City.mmdb";
		} else if (args.length == 1) {
			pathToGeoIP2City = args[0];
		} else {
			throw new IllegalArgumentException("You need to give GeoLite2 City database path");
		}

		// Now we create the topology
		TopologyBuilder builder = new TopologyBuilder();

		// Kafka spout config
		KafkaSpoutConfig<String, String> kafkaConfig = KafkaSpoutConfig.builder("localhost:9094", "logs")
				.setFirstPollOffsetStrategy(EARLIEST).setProp(ConsumerConfig.GROUP_ID_CONFIG, "logs_group").build();

		// Set the kafka spout
		builder.setSpout("KafkaSpout", new KafkaSpout<>(kafkaConfig), 1);

		// Bolt that convert Ip to a Country name
		builder.setBolt("IpToCountry", new UserInformationGetterBolt(pathToGeoIP2City), 1).shuffleGrouping("KafkaSpout");

		// Bolt that extracts keywords used in the search
		builder.setBolt("Keyword", new KeyWordIdentifierBolt(), 1).shuffleGrouping("IpToCountry");

		// Bolt that transforms data in a way that can be used by the default tuple
		// mapper of EsBolt
		builder.setBolt("TupleToJson", new EsTransformerBolt(), 1).shuffleGrouping("Keyword");

		// Elasticsearch Bolt config
		Header[] headers = { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
		EsConfig esConfig = new EsConfig(new String[] { "http://localhost:9200" }).withDefaultHeaders(headers);
		EsTupleMapper tupleMapper = new DefaultEsTupleMapper();

		// Set the Elasticsearch bolt
		builder.setBolt("PersistenceBolt", new EsIndexBolt(esConfig, tupleMapper), 1).shuffleGrouping("TupleToJson");

		// Set Storm cluster configuration
		Config conf = new Config();
		conf.setNumWorkers(1);

		if (debug) {
			// Run Storm topology in local mode (DEBUG)
			try (LocalCluster cluster = new LocalCluster()) {
				cluster.submitTopology(topologyName, conf, builder.createTopology());
				// Wait for some time before exiting
				System.out.println("Waiting to consume from kafka");
				Thread.sleep(6000000);

				cluster.killTopology(topologyName);
				cluster.shutdown();
				cluster.close();
			} catch (Exception e) {
				throw e;
			}
		} else {
			// Run Storm topology in the cluster (PRODUCTION)
			StormSubmitter.submitTopology(topologyName, conf, builder.createTopology());
		}
	}
}
