package org.example;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.Skier;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

public class Consumer {

  private final static String QUEUE_NAME = "SkierQueue";
//  static ConcurrentHashMap<Integer, CopyOnWriteArrayList<Skier>> map = new ConcurrentHashMap<>();
  private static JedisPool jedisPool;
  private final static String REDIS_HOST = "localhost";
  private final static Integer REDIS_PORT = 6379;
  private final static Integer POOL_SIZE = 200;

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("172.31.18.111");
//    factory.setHost("localhost");
    Connection connection = factory.newConnection();

    RMQChannelFactory chanFactory = new RMQChannelFactory (connection);
    RMQChannelPool pool = new RMQChannelPool(POOL_SIZE, chanFactory);
    Gson gson = new Gson();

    jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);
    JedisPoolConfig config = new JedisPoolConfig();
    config.setMaxTotal(200);
    jedisPool.setConfig(config);

    Runnable runnable = () -> {
        try {
          Channel channel;
          // get a channel from the pool
          channel = pool.borrowObject();

          // publish message
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
          System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
          // accept only 1 unacknowledged message
//          channel.basicQos(1);


          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            Skier skier = gson.fromJson(message, Skier.class);

//            map.putIfAbsent(skier.getSkierID(), new CopyOnWriteArrayList<>());
//            map.get(skier.getSkierID()).add(skier);

            createHashEntry(skier);


            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            System.out.println(" [x] Received '" + message + "'");
          };
          channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
          pool.returnObject(channel);

        } catch (Exception ex) {
          ex.printStackTrace();
        }
    };
    for (int i = 0; i < POOL_SIZE; i++) {
      Thread thread = new Thread(runnable);
      thread.start();
    }
  }

  private static void createHashEntry(Skier skier) {
    Map<String, String> map = new HashMap<>();

    map.put("skierId",  String.valueOf(skier.getSkierID()));
    map.put("resortId", String.valueOf(skier.getResortID()));
    map.put("liftId", String.valueOf(skier.getLiftID()));
    map.put("seasonId", String.valueOf(skier.getSeasonID()));
    map.put("dayId", String.valueOf(skier.getDayID()));
    map.put("time", String.valueOf(skier.getTime()));

    String mapKey = "resort_ID_" + skier.getResortID() + "_day_ID_" + skier.getDayID() + "_skier_ID_" + skier.getSkierID() + "_time_" + skier.getTime();
    try (Jedis jedis = jedisPool.getResource()) {
      // The previous data model
      jedis.hmset(mapKey, map);

      // Data model for GET-1: get number of unique skiers at resort/season/day
      // We can use Redis.scard method to get the number of elements of a single key, and that would be the result we need
      // key-value pair: (GET1:resortID_1_seasonID_2022_dayID_1, skierID)
      jedis.sadd("GET1:resortID_" + skier.getResortID() + "_seasonID_" + skier.getSeasonID() + "_dayID_" + skier.getDayID(),
              String.valueOf(skier.getSkierID()));

      // Data model for GET-2: get the total vertical for the skier for the specified ski day
      // key-value pair: (GET2:resortID_1_seasonID_2022_dayID_1_skierID_100, numberOfVerticalOnDay1)
      jedis.incrBy("GET2:resortID_" + skier.getResortID() + "_seasonID_" + skier.getSeasonID() + "_dayID_"
              + skier.getDayID() + "_skierID_" + skier.getSkierID(), skier.getDayID() * 10);

      // Data model for GET-3: get the total vertical for the skier.
      // key-value pair: (GET3:totalVerticalForSkier_1, numberOfAllVerticalsForSkier1)
      jedis.incrBy("GET3:totalVerticalForSkier_" + skier.getSkierID(), skier.getLiftID() * 10);
    }

  }

}
