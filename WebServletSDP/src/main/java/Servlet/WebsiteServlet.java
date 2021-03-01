package Servlet;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.net.URL;

@WebServlet("/web")
public class WebsiteServlet extends HttpServlet {
    static String PAGE_HEADER = "<html><head>" +
                                    "<meta charset='UTF-8'>" +
                                    "<title>SDP</title>" +
                                    "<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css' integrity='sha384-TX8t27EcRE3e/ihU7zmQxVncDAy5uIKz4rEkgIXeMed4M0jlfIDPvg6uqKI2xXr2' crossorigin='anonymous'>" +
                                "</head><body style='background-color: #31343F'>";
    static String PAGE_STYLE = "<style>" +
                                    "html {height: 100%;}" +
                                    "body{height: auto;width: 100%;margin: 0;background-size: 100% 100%;background-repeat: no-repeat;}" +
                                    "#Container_central{margin-top: 100px;}" +
                                    "#title {text-align: center;color: white;font-weight: bold;margin-bottom: 50px;}" +
                                    "#submit_btn{text-align: center;}" +
                                "</style>";

    static String PAGE_BODY = "<div class='container' id = 'Container_central'>" +
                                "<div class='row'>" +
                                    "<div class='col-sm-12'>" +
                                        "<h1 id = 'title'><b>ITEMS PAGE</b></h1>" +
                                        "<table class='table' style='width:100%'>" +
                                            "<thead class='thead-light'>" +
                                                "<tr>" +
                                                    "<th scope='col'>ID</th>" +
                                                    "<th scope='col'>Name</th>" +
                                                    "<th scope='col'>Quantity</th>" +
                                                    "<th scope='col'>Description</th>" +
                                                 "</tr>" +
                                             "</thead>" +
                                        "<tbody>";

    static String PAGE_FOOTER = "</tbody>" +
                            "</table>" +
                        "</div>" +
                    "</div>" +
                "</div>" +
            "<script src='https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/js/bootstrap.bundle.min.js' integrity='sha384-ho+j7jyWK8fNQe+A12Hb8AhRq26LrZ/JpcUGGOn+Y7RsweNrtN/tE3MoK7ZeZDyx' crossorigin='anonymous'></script>" +
            "</body></html>";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        writer.println(PAGE_HEADER);
        writer.println(PAGE_STYLE);
        writer.println(PAGE_BODY);

        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://srv:8080/items/?get=item").openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String stringJson = reader.readLine();
            JSONObject object = new JSONObject(stringJson);
            JSONArray items = object.getJSONArray("Items");

            for(int i = 0; i < items.length(); i++){
                JSONObject itemObject = items.getJSONObject(i);
                int idItem = itemObject.getInt("id");
                String name = itemObject.getString("name");
                int quantity = itemObject.getInt("quantity");
                String description = itemObject.getString("description");

                writer.println("<tr>" +
                                    "<th class='table-light' scope='row'>"+ idItem +"</th>" +
                                    "<td class='table-light'>"+ name +"</td>" +
                                    "<td class='table-light'>"+ quantity +"</td>" +
                                    "<td class='table-light'>"+ description +"</td>" +
                                "</tr>");
            }

            writer.println(PAGE_FOOTER);
            writer.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

