package org.sds;



import com.google.gson.JsonObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ResortServlet", value = "/resorts/*")
public class ResortServlet extends HttpServlet {

    // TODO: change REDIS_HOST
    private final static String REDIS_HOST = "localhost";
    private final static Integer REDIS_PORT = 6379;
    private String skierID;
    private String dayID;
    private String resortID;
    private String seasonID;

    private JedisPool jedisPool;
    private Jedis jedis;


    @Override
    public void init() throws ServletException {
        jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(200);
        jedisPool.setConfig(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        formatPayload(urlPath);
        String[] urlParts = urlPath.split("/");

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing parameters");
            return;
        }

        Boolean isGet1 = isUrlValidGet1(urlParts);
        // check if match GET1
        if (!isGet1) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("no such path found");
        }

        if (isGet1) {
            res.setStatus(HttpServletResponse.SC_OK);
            jedis = jedisPool.getResource();
            String key = getKeyForGET1(urlParts);
            // Get the total number of elements of the key
            long numberOfSkiers = jedis.scard(key);
            JsonObject result = new JsonObject();
            result.addProperty("time", "Mission Ridge");
            result.addProperty("numSkiers", numberOfSkiers);
            res.getWriter().write(String.valueOf(result));
        }
    }


    // key-value pair: (GET1:resortID_1_seasonID_2022_dayID_1, skierID)
    private String getKeyForGET1(String[] urlParts) {
        return "GET1:resortID_" + this.resortID + "_seasonID_" + this.seasonID + "_dayID_" + this.dayID;
    }


    /**
     * Check if the urlParts match the GET1 path
     * @param urlParts
     * @return
     */
    private boolean isUrlValidGet1(String[] urlParts) {
        // urlPath  = "/1/seasons/2019/day/1/skiers"
        // urlParts = [, 1, seasons, 2019, day, 1, skiers]
        // Handle /resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers
        if (urlParts.length == 7) {
            if (!urlParts[0].equals("")
                    || !urlParts[2].equals("seasons")
                    || !urlParts[4].equals("day")
                    || !urlParts[6].equals("skiers")) {
                return false;
            }
        }
        return urlParts.length == 7;
    }


    private void formatPayload(String urlPath) {
        String[] urlParams = urlPath.split("/");
        this.resortID = urlParams[2];
        this.seasonID = urlParams[4];
        this.dayID = urlParams[6];
        this.skierID = urlParams[8];
    }

}