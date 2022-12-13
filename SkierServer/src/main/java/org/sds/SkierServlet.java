package org.sds;


import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import com.google.gson.Gson;


import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

import org.sds.model.Skier;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@WebServlet(name = "SkierServlet", value = "/Skier/*")
public class SkierServlet extends HttpServlet {
    private final static String QUEUE_NAME = "SkierQueue";
    private final static String QUEUE_URL = "52.43.76.130";

    // TODO: change REDIS_HOST
    private final static String REDIS_HOST = "localhost";
    private final static Integer REDIS_PORT = 6379;
    private RMQChannelPool channelPool;
    private final static Integer POOL_SIZE = 200;
    private String skierID;
    private String dayID;
    private String resortID;
    private String seasonID;

    private JedisPool jedisPool;
    private Jedis jedis;


    @Override
    public void init() throws ServletException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(QUEUE_URL);
        RMQChannelFactory channelFactory;
        try {
            channelFactory = new RMQChannelFactory(factory.newConnection());
            jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(200);
            jedisPool.setConfig(config);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        this.channelPool = new RMQChannelPool(POOL_SIZE, channelFactory);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        String[] urlParts = urlPath.split("/");

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing parameters");
            return;
        }

        Boolean isGet2 = isUrlValidGet2(urlParts);
        Boolean isGet3 = isUrlValidGet3(urlParts);
        // check if match GET2 OR GET3
        if (!isGet2 && !isGet3) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("no such path found");
        }

        if (isGet2) {
            res.setStatus(HttpServletResponse.SC_OK);
            jedis = jedisPool.getResource();
            String key = getKeyForGET2(urlParts);
            String value = String.valueOf(jedis.get(key));
            res.getWriter().write(value);
        } else if (isGet3) {
            res.setStatus(HttpServletResponse.SC_OK);
            jedis = jedisPool.getResource();
            String key = getKeyForGET3(urlParts);
            String value = String.valueOf(jedis.get(key));
            res.getWriter().write(value);
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        String urlPath = request.getPathInfo();
        formatPayload(urlPath);
        Gson gson = new Gson();
        try {
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }

//          set formatted load to send to rabbitmq queue

            Skier skier = gson.fromJson(sb.toString(), Skier.class);
            skier.setSkierID(Integer.parseInt(this.skierID));
            skier.setResortID(Integer.parseInt(this.resortID));
            skier.setSeasonID(Integer.parseInt(this.seasonID));
            skier.setDayID(Integer.parseInt(this.dayID));
            String message = gson.toJson(skier);

//          send message to rabbitmq queue.
            sendToBroker(message);

            System.out.println(message);
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();
            out.print(skier.toString());
            out.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }


    }

    // key-value pair: (GET2:resortID_1_seasonID_2022_dayID_1_skierID_100, numberOfVerticalOnDay1)
    private String getKeyForGET2(String[] urlParts) {
        return "GET2:resortID_" + this.resortID + "_seasonID_" + this.seasonID + "_dayID_" + this.dayID + "_skierID_" + this.skierID;
    }

    // key-value pair: (GET3:totalVerticalForSkier_1, numberOfAllVerticalsForSkier1)
    private String getKeyForGET3(String[] urlParts) {
        return "GET3:totalVerticalForSkier_" + this.skierID;
    }

    /**
     * Check if the urlParts match the GET2 path
     * @param urlParts
     * @return
     */
    private boolean isUrlValidGet2(String[] urlParts) {
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        // Handle /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
        if (urlParts.length == 8) {
            if (!urlParts[0].equals("")
                    || !urlParts[2].equals("seasons")
                    || !urlParts[4].equals("days")
                    || !urlParts[6].equals("skiers")) {
                return false;
            }
        }
        return urlParts.length == 8;
    }

    /**
     * Check if the urlParts match the GET3 path
     * @param urlParts
     * @return
     */
    private boolean isUrlValidGet3(String[] urlParts) {
        // urlPath  = "/1/vertical"
        // urlParts = [, 1, vertical]
        // Handle /skiers/{skierID}/vertical
        if (urlParts.length == 3) {
            if (!urlParts[0].equals("") || !urlParts[2].equals("vertical")) {
                return false;
            }
        }
        return urlParts.length == 3;
    }

    private void formatPayload(String urlPath) {

        String[] urlParams = urlPath.split("/");
        this.resortID = urlParams[2];
        this.seasonID = urlParams[4];
        this.dayID = urlParams[6];
        this.skierID = urlParams[8];
    }

    private void sendToBroker(String message) throws Exception {
        Channel channel = this.channelPool.borrowObject();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
        channelPool.returnObject(channel);
    }
}