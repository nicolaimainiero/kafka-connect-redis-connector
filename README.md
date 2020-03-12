# Sample Kafka Connect Source Connector for Redis

This repository contains a sample implementation for a simple Kafka Connect Source Connector for Redis. 

## Try it

1. Build the project with `mvn clean package`
1. Unzip `kafka-connect-redis-connector-1.1.zip` found in the target folder
1. Start the Kafka stack with `docker-compose up`
1. Open http://localhost:8888/ and create a new Redis Connector with following properties
    ```properties
    name=RedisSourceConnector
    connector.class=de.mainiero.kafka.connect.RedisSourceConnector
    tasks.max=1
    topic=sample-redis
    host=redis
    port=6379
    channel=sample
    ```
1. Connect to Redis cluster `docker run -it --rm redis redis-cli -h docker.for.mac.localhost -p 6379`
1. Send message to channel `sample`
1. Open http://localhost:9021 and go to Cluster 1 / Topics, there should be now a *sample-redis* topic containing the message sent to Redis.
                             
