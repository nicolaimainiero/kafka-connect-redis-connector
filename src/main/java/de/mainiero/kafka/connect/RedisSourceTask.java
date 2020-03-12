package de.mainiero.kafka.connect;

import com.google.common.collect.ImmutableMap;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RedisSourceTask extends SourceTask implements RedisPubSubListener<String, String> {

    private static final Logger LOG = LoggerFactory.getLogger(RedisSourceTask.class);
    private Deque<SourceRecord> messageQueue;

    private String topic;
    private RedisClient redisClient;
    private StatefulRedisPubSubConnection<String, String> connection;

    @Override
    public String version() {
        return new RedisSourceConnector().version();
    }

    @Override
    public void start(final Map<String, String> props) {
        LOG.info("Start new RedisSourceTask and open connection to Redis.");
        final String host = props.get(RedisSourceConnector.HOST);
        final int port = Integer.parseInt(props.get(RedisSourceConnector.PORT));
        final String channel = props.get(RedisSourceConnector.CHANNEL);
        topic = props.get(RedisSourceConnector.TOPIC_CONFIG);
        redisClient = RedisClient.create(RedisURI.Builder.redis(host, port).build());
        connection = redisClient.connectPubSub();
        connection.addListener(this);
        final RedisPubSubCommands<String, String> sync = connection.sync();
        sync.subscribe(channel);
        messageQueue = new ConcurrentLinkedDeque<>();
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        final List<SourceRecord> records = new ArrayList<>(1024);
        int count = 0;
        SourceRecord record;
        while (count <= 1024 && null != (record = messageQueue.poll())) {
            records.add(record);
            count++;
            LOG.info("Answering poll() with {} records.", count);
        }
        return records;
    }

    @Override
    public void stop() {
        connection.close();
        redisClient.shutdown();
    }

    @Override
    public void message(final String channel, final String message) {
        LOG.info("Recieved message from redis.");
        final Map<String, ?> sourcePartition = ImmutableMap.of("channel", channel);
        final Map<String, ?> sourceOffset = ImmutableMap.of();
        final SourceRecord record = new SourceRecord(sourcePartition, sourceOffset, topic, SchemaBuilder.STRING_SCHEMA, message);
        messageQueue.add(record);
    }

    @Override
    public void message(final String pattern, final String channel, final String message) {
        LOG.info("Method not implemented.");

    }

    @Override
    public void subscribed(final String channel, final long count) {
        LOG.info("Method not implemented.");

    }

    @Override
    public void psubscribed(final String pattern, final long count) {
        LOG.info("Method not implemented.");
    }

    @Override
    public void unsubscribed(final String channel, final long count) {
        LOG.info("Method not implemented.");
    }

    @Override
    public void punsubscribed(final String pattern, final long count) {
        LOG.info("Method not implemented.");

    }
}
