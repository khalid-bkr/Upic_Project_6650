package org.example;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.Skier;

public class Consumer {

  private final static String QUEUE_NAME = "SkierQueue";
  static ConcurrentHashMap<Integer, CopyOnWriteArrayList<Skier>> map = new ConcurrentHashMap<>();
//  static ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
  private final static Integer POOL_SIZE = 200;

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("172.31.18.111");
    Connection connection = factory.newConnection();

    RMQChannelFactory chanFactory = new RMQChannelFactory (connection);
    RMQChannelPool pool = new RMQChannelPool(POOL_SIZE, chanFactory);

    Gson gson = new Gson();

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

            map.putIfAbsent(skier.getSkierID(), new CopyOnWriteArrayList<>());
            map.get(skier.getSkierID()).add(skier);
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
}
