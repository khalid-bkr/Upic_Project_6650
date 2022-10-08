package org.sds;

import com.google.gson.Gson;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import org.sds.model.Skier;

@WebServlet(name = "SkierServlet", value = "/Skier/*")
public class SkierServlet extends HttpServlet {

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
      Gson gson = new Gson();

//      String skierJsonString = new Gson().toJson();

      try {
          StringBuilder sb = new StringBuilder();
          String s;
          while ((s = request.getReader().readLine()) != null) {
              sb.append(s);
          }
          Skier skier = gson.fromJson(sb.toString(), Skier.class);
          response.setStatus(HttpServletResponse.SC_OK);
          PrintWriter out = response.getWriter();
          out.print(skier.toString());
          out.flush();
      } catch (Exception ex) {
          ex.printStackTrace();
          response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      }


  }
}
