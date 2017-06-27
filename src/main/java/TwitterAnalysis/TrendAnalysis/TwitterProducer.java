package TwitterAnalysis.TrendAnalysis;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class TwitterProducer {

	private static final String topic = "twittertopic";

	public static void run() throws InterruptedException {

		String consumerKey = "Jzp0bOisECssdJs0H406uKKEW";
	    String consumerSecret = "y87gTNI11AtGpjIqVrSkON5kn5IW05mutH53KqbhogsUY25W3R";
	    String accessToken = "57676049-I4twV5T0bSCov74eJLIjZUcnnZc4MxGVLuLktUWPZ";
	    String accessTokenSecret = "8ULFKtomSdXMT2ppO1i3VbJYfC1Q8rm4TjjqFMmopDM3H";
		Properties properties = new Properties();
		properties.put("metadata.broker.list", "52.42.244.153:9092");
		properties.put("serializer.class", "kafka.serializer.StringEncoder");
		//properties.put("client.id","camus");
		ProducerConfig producerConfig = new ProducerConfig(properties);
		kafka.javaapi.producer.Producer<String, String> producer = new kafka.javaapi.producer.Producer<String, String>(producerConfig);

		BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
		StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
		// add some track terms
		endpoint.trackTerms(Lists.newArrayList("#bigdata"));

		Authentication auth = new OAuth1(consumerKey, consumerSecret, accessToken, 	accessTokenSecret);
		// Authentication auth = new BasicAuth(username, password);

		// Create a new BasicClient. By default gzip is enabled.
		Client client = new ClientBuilder()
				.hosts(Constants.STREAM_HOST)
				.endpoint(endpoint)
				.authentication(auth)
				.processor(new StringDelimitedProcessor(queue))
				.build();

		// Establish a connection
		client.connect();

		// Do whatever needs to be done with messages
		for (int msgRead = 0; msgRead < 1000; msgRead++) {
			KeyedMessage<String, String> message = null;
			try {
				String msg=queue.take();
				//System.out.println(msg);
				message = new KeyedMessage<String, String>(topic, queue.take());
				System.out.println(message);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			producer.send(message);
		}
		producer.close();
		client.stop();

	}

	public static void main(String[] args) {
		try {
			TwitterProducer.run();
		} catch (InterruptedException e) {
			System.out.println(e);
		}
	}
}
