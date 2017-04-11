package cn.uway.util.kafka;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.commons.codec.StringEncoder;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;

public class KafkaIgpProducer {

	private static Map<String, KafkaIgpProducer> mapKafkaIgpProducers = new HashMap<String, KafkaIgpProducer>();

	private static final ILogger LOGGER = LoggerManager
			.getLogger(KafkaIgpProducer.class);

//	private static String zkServerList = AppContext.getBean(
//			"kafkaZookeeperServerList", String.class);
//
//	private static String brokerSvrList = AppContext.getBean(
//			"kafkaBrokerServerList", String.class);
	
	private static Object sendLock_ = new Object();

	public synchronized static KafkaIgpProducer getProducer(String brokerSvrList, String topic) {
		KafkaIgpProducer producer = mapKafkaIgpProducers.get(topic);
		if (producer == null) {
			try {
				producer = new KafkaIgpProducer(brokerSvrList, topic);
				mapKafkaIgpProducers.put(topic, producer);
			} catch (Exception e) {
				LOGGER.error("连接kafka服务器失败.", e);
				return null;
			}
		}

		return producer;
	}
	
	private String brokerSvrList;
	
	private String topic;

	private Properties properties = null;

	private Producer<Long, String> producer = null;

	private KafkaIgpProducer(String brokerSvrList, String topic) throws Exception {
		
		this.brokerSvrList = brokerSvrList;
		this.topic = topic;
		this.properties = new Properties();
//		properties.put("zookeeper.connect", zkServerList);
		properties.put("serializer.class", StringEncoder.class.getName());
		properties.put("metadata.broker.list", brokerSvrList);
		
		LOGGER.debug("kafka推送服务器，broke地址:{} topic:{}", this.brokerSvrList, this.topic);
		producer = new Producer<Long, String>(new ProducerConfig(properties));
	}

	public void send(String msg) {
		KeyedMessage<Long, String> kMsg = new KeyedMessage<Long, String>(topic,
				msg);
		
		synchronized (sendLock_) {
			producer.send(kMsg);
		}
	}
	
	public void send(String[] msgs) {
		List<KeyedMessage<Long, String>> kMsgs = new LinkedList<KeyedMessage<Long, String>>();
		for (String msg: msgs) {
			System.out.println(msg);
			kMsgs.add(new KeyedMessage<Long, String>(topic,	msg));
		}
		
		synchronized (sendLock_) {
			for (KeyedMessage<Long, String> kMsg : kMsgs) {
				producer.send(kMsg);
			}
		}
	}

	public void close() {
		if (producer != null) {
			//producer.close();
			//producer = null;
		}
	}

}
