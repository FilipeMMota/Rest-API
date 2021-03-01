package Servlets;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

@WebServlet("/items/*")
public class ItemServlet extends HttpServlet {

    private static Connection c;
    private static Statement state = null;

    private void DatabaseConnection(){
        try{
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://db:5432/sdp",
                            "postgres", "foobar");
            c.setAutoCommit(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        DatabaseConnection();

        try {
            String parameter = req.getParameter("get");

            if(parameter.equals("item")){
                state = c.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = state.executeQuery ("SELECT * FROM item;");

                JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
                JsonArrayBuilder jsonArray = Json.createArrayBuilder();

                if(rs.next()){
                    rs.previous();
                    while(rs.next()){
                        JsonObjectBuilder itemBuilder = Json.createObjectBuilder();
                        int id = rs.getInt("iditem");
                        String name = rs.getString("name");
                        int quantity = rs.getInt("quantity");
                        String description = rs.getString("description");
                        itemBuilder.add("id", id);
                        itemBuilder.add("name", name);
                        itemBuilder.add("quantity", quantity);
                        itemBuilder.add("description", description);
                        jsonArray.add(itemBuilder);
                    }
                    jsonBuilder.add("Items", jsonArray);
                }else{
                   jsonBuilder.add("msg", "there aren't items registered");
                }
                JsonWriter jsonWriter = Json.createWriter(resp.getWriter());
                jsonWriter.writeObject(jsonBuilder.build());
                jsonWriter.close();
                rs.close();

            }else if(parameter.equals("deposited")){
                state = c.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = state.executeQuery( "SELECT * FROM item;");

                JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
                JsonArrayBuilder jsonArray = Json.createArrayBuilder();
                loop:
                if(rs.next()){
                    rs.previous();
                    while ( rs.next() ) {
                        JsonObjectBuilder itemBuilder = Json.createObjectBuilder();
                        int id = rs.getInt("iditem");
                        String  name = rs.getString("name");
                        int quantity = rs.getInt("quantity");
                        String description = rs.getString("description");
                        if(quantity > 0){
                            itemBuilder.add("id", id);
                            itemBuilder.add("name", name);
                            itemBuilder.add("quantity", quantity);
                            itemBuilder.add("description", description);
                            jsonArray.add(itemBuilder);
                        }
                    }
                    jsonBuilder.add("items", jsonArray);
                }else{
                    jsonBuilder.add("msg", "there aren't items registered");
                }
                JsonWriter jsonWriter = Json.createWriter(resp.getWriter());
                jsonWriter.writeObject(jsonBuilder.build());
                jsonWriter.close();
                rs.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        JsonReader jsonReader = Json.createReader(req.getInputStream());
        String name = jsonReader.readObject().getString("name");

        DatabaseConnection();

        try {
            PreparedStatement pstmt = c.prepareStatement("SELECT * FROM item where name = ?;");
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                jsonBuilder.add("msg", "That item already exists");
            } else {
                try {
                    pstmt = c.prepareStatement("INSERT INTO item (name) " + "VALUES (?);");
                    pstmt.setString(1, name);
                    pstmt.executeUpdate();
                    pstmt.close();
                    jsonBuilder.add("msg", "Item Registered with success");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            JsonWriter jsonWriter = Json.createWriter(resp.getWriter());
            jsonWriter.writeObject(jsonBuilder.build());
            jsonWriter.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject jsonObject = Json.createReader(req.getReader()).readObject();
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

        DatabaseConnection();

        try {
            String parameter = req.getParameter("alter");

            if(parameter.equals("deposit")){
                String name = jsonObject.getString("name");
                int quantity = jsonObject.getInt("quantity");
                PreparedStatement pstmt = c.prepareStatement("SELECT * FROM item where name = ?;");
                pstmt.setString(1, name);
                ResultSet rs = pstmt.executeQuery();
                if(rs.next()){
                    String itemTable = rs.getString("name");
                    int quantityItem = rs.getInt("quantity");
                    if(name.equals(itemTable)){
                        pstmt = c.prepareStatement("UPDATE item set quantity = ? + ? where name = ?;");
                        pstmt.setInt(1, quantity);
                        pstmt.setInt(2, quantityItem);
                        pstmt.setString(3, name);
                        pstmt.executeUpdate();
                        pstmt.close();
                        jsonBuilder.add("msg", "Item updated with success");
                    }
                }else{
                    jsonBuilder.add("msg", "There isn't an item registered with that name");
                }
            }else if(parameter.equals("description")){
                String name = jsonObject.getString("name");
                String description = jsonObject.getString("description");
                PreparedStatement pstmt = c.prepareStatement( "SELECT name FROM item where name = ?;");
                pstmt.setString(1, name);
                ResultSet rs = pstmt.executeQuery();
                if(rs.next()){
                    String nameItem = rs.getString("name");
                    if(name.equals(nameItem)){
                        pstmt = c.prepareStatement("UPDATE item set description = ? where name = ?;");
                        pstmt.setString(1, description);
                        pstmt.setString(2, name);
                        pstmt.executeUpdate();
                        pstmt.close();
                        jsonBuilder.add("msg", "Description has been updated");
                    }
                }else{
                    jsonBuilder.add("msg", "There isn't an item registered with that name");
                }
            }

            JsonWriter jsonWriter = Json.createWriter(resp.getWriter());
            jsonWriter.writeObject(jsonBuilder.build());
            jsonWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        JsonObject jsonObject = Json.createReader(req.getReader()).readObject();
        String name = jsonObject.getString("name");

        DatabaseConnection();

        try {
            PreparedStatement pstmt = c.prepareStatement( "SELECT * FROM item where name = ?;");
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            itFoundItem:
            if(rs.next()) {
                String itemItem = rs.getString("name");
                int quantity = rs.getInt("quantity");
                if (name.equals(itemItem)){
                    if(quantity == 0){
                        pstmt = c.prepareStatement("DELETE from item where name = ?;");
                        pstmt.setString(1, name);
                        pstmt.executeUpdate();
                        jsonBuilder.add("msg", "Item deleted with success");
                    }else{
                        jsonBuilder.add("msg", "That item cannot be deleted because it has been deposited");
                    }
                    break itFoundItem;
                }
            }else{
                jsonBuilder.add("msg", "There isn't an item with that name");
            }

            JsonWriter jsonWriter = Json.createWriter(resp.getWriter());
            jsonWriter.writeObject(jsonBuilder.build());
            jsonWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
