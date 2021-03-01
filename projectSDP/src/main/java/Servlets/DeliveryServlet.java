package Servlets;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

@WebServlet("/delivery/*")
public class DeliveryServlet extends HttpServlet {

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

        try{
            state = c.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = state.executeQuery( "SELECT * FROM delivery;");

            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
            JsonArrayBuilder jsonArray = Json.createArrayBuilder();

            if(rs.next()){
                rs.previous();
                while ( rs.next() ) {
                    JsonObjectBuilder deliveryBuilder = Json.createObjectBuilder();

                    int id = rs.getInt("iddelivery");
                    int item = rs.getInt("iditemdelivery");
                    String nameDelivery = rs.getString("name");
                    int quantity = rs.getInt("quantity");
                    String location = rs.getString("location");

                    deliveryBuilder.add("ID", id);
                    deliveryBuilder.add("Name", nameDelivery);
                    deliveryBuilder.add("Item", item);
                    deliveryBuilder.add("Stock", quantity);
                    deliveryBuilder.add("Location", location);

                    jsonArray.add(deliveryBuilder);
                }
                jsonBuilder.add("Deliveries", jsonArray);

            }else{
                jsonBuilder.add("msg", "there aren't deliveries registered");
            }

            JsonWriter jsonWriter = Json.createWriter(resp.getWriter());
            jsonWriter.writeObject(jsonBuilder.build());
            jsonWriter.close();
            rs.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        JsonObject jsonObject = Json.createReader(req.getReader()).readObject();
        JsonArray arrayBuilder = jsonObject.getJsonArray("Delivery");

        DatabaseConnection();

        try{
            loop:
            for (int i = 0; i < arrayBuilder.size(); ++i) {
                JsonObject deliveries = arrayBuilder.getJsonObject(i);
                int idItem = deliveries.getInt("idItem");
                String name = deliveries.getString("name");
                int quantity = deliveries.getInt("quantity");
                String location = deliveries.getString("location");

                PreparedStatement pstmt = c.prepareStatement("SELECT * FROM item where iditem = ?;");
                pstmt.setInt(1, idItem);
                ResultSet rs = pstmt.executeQuery();
                if(rs.next()){
                    int idItemTable = rs.getInt("iditem");
                    int quantityItem = rs.getInt("quantity");

                    if (idItem == idItemTable) {
                        if(quantityItem <= quantity) {
                            jsonBuilder.add("msg", "That item doesn't have that much stock");
                            break loop;
                        }else{
                            pstmt = c.prepareStatement("INSERT INTO delivery (iditemdelivery, name, quantity, location) " + "VALUES (?, ?, ?, ?);");//nas entregas decidimos dar um nome à entrega e os items que têm
                            pstmt.setInt(1, idItem);                                                                                       //o mesmo nome de entrega pertencem à mesma entrega.
                            pstmt.setString(2, name);
                            pstmt.setInt(3, quantity);
                            pstmt.setString(4, location);
                            pstmt.executeUpdate();
                            pstmt = c.prepareStatement("UPDATE item set quantity = ? - ? where iditem = ?;");
                            pstmt.setInt(1, quantityItem);
                            pstmt.setInt(2, quantity);
                            pstmt.setInt(3, idItem);
                            pstmt.executeUpdate();
                            pstmt.close();
                        }
                    }
                    if(i == (arrayBuilder.size() - 1)){
                        jsonBuilder.add("msg", "Delivery has been created");
                    }
                }else{
                    jsonBuilder.add("msg", "There isn't an items with that ID");
                    break loop;
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
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        JsonObject jsonObject = Json.createReader(req.getReader()).readObject();
        String name = jsonObject.getString("name");
        String location = jsonObject.getString("location");

        DatabaseConnection();

        try{
            PreparedStatement pstmt = c.prepareStatement( "SELECT name FROM delivery where name = ?;");
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                String nameDelivery = rs.getString("name");
                if(name.equals(nameDelivery)){
                    pstmt = c.prepareStatement("UPDATE delivery set location = ? where name = ?;");
                    pstmt.setString(1, location);
                    pstmt.setString(2, name);
                    pstmt.executeUpdate();
                    pstmt.close();
                    jsonBuilder.add("msg", "location of the delivery has been updated");
                }else{
                    jsonBuilder.add("msg", "There isn't a delivery with that name");
                }
            }else{
                jsonBuilder.add("msg", "There isn't a delivery with that name");
            }
            JsonWriter jsonWriter = Json.createWriter(resp.getWriter());
            jsonWriter.writeObject(jsonBuilder.build());
            jsonWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
