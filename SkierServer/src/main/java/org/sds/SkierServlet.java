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

@WebServlet(name = "SkierServlet", value = "/Skier/*")
public class SkierServlet extends HttpServlet {
    private final static String QUEUE_NAME = "SkierQueue";
    private final static String QUEUE_URL = "52.43.76.130";
    private RMQChannelPool channelPool;
    private final static Integer POOL_SIZE = 200;
    private String skierID;
    private String dayID;
    private String resortID;
    private String seasonID;


    @Override
    public void init() throws ServletException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(QUEUE_URL);
        RMQChannelFactory channelFactory;
        try {
            channelFactory = new RMQChannelFactory(factory.newConnection());
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        this.channelPool = new RMQChannelPool(POOL_SIZE, channelFactory);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            res.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts`
            res.getWriter().write("It works!");
        }
    }

    private boolean isUrlValid(String[] urlPath) {
        // TODO: validate the request url path according to the API spec
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        return true;
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