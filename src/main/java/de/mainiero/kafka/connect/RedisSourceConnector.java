package de.mainiero.kafka.connect;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisSourceConnector extends SourceConnector {

    public static final String TOPIC_CONFIG = "topic";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String CHANNEL = "channel";

    private String topic;
    private String host;
    private int port;
    private String channel;

    private static final ConfigDef CONFIG_DEF = new ConfigDef()
        .define(TOPIC_CONFIG, Type.STRING, null, Importance.HIGH, "The topic to publish data to")
        .define(HOST, Type.STRING, null, Importance.HIGH, "The host of redis server.")
        .define(PORT, Type.STRING, null, Importance.HIGH, "The port of the redis server")
    .define(CHANNEL, Type.STRING, null, Importance.HIGH, "The channel this connector will listen to");

    @Override
    public void start(final Map<String, String> props) {
        final AbstractConfig parsedConfig = new AbstractConfig(CONFIG_DEF, props);
        topic = parsedConfig.getString(TOPIC_CONFIG);
        host = parsedConfig.getString(HOST);
        port = Integer.parseInt(parsedConfig.getString(PORT));
        channel = parsedConfig.getString(CHANNEL);
    }

  	@Override
    public Class<? extends Task> taskClass() {
        return RedisSourceTask.class; 
    }

    @Override
    public List<Map<String, String>> taskConfigs(final int maxTasks) {
        final ArrayList<Map<String, String>> configs = new ArrayList<>();
        final Map<String, String> config = new HashMap<>();
        config.put(HOST, host);
        config.put(TOPIC_CONFIG, topic);
        config.put(PORT, String.valueOf(port));
        config.put(CHANNEL, channel);
        configs.add(config);
        return configs;
    }

    @Override
    public void stop() {

    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public String version() { 
        return AppInfoParser.getVersion();
    }


}
