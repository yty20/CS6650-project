package org.CS6650;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@WebServlet(name = "SkiersServlet", urlPatterns = {"/skiers/*"})
public class SkiersServlet extends HttpServlet {
    public static final int DAYID_UPPER_BOUND = 366;
    private final Gson gson = new Gson();
    private ChannelPool channelPool;
    private Connection connection;

    @Override
    public void init() throws ServletException {
        super.init();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("52.32.52.22");
        factory.setUsername("myuser");
        factory.setPassword("mypassword");
        try {
            connection = factory.newConnection();
            channelPool = new ChannelPool(connection);
        } catch (IOException | TimeoutException e) {
            throw new ServletException("Could not open connection to RabbitMQ", e);
        }
    }

    @Override
    public void destroy(){
        super.destroy();
        try {
            channelPool.close();
            if(connection != null){
                connection.close();
            }
        } catch (IOException | TimeoutException e) {
            System.err.println("Could not close RabbitMQ connection");
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String[] pathParts = pathInfo.split("/");

        if (pathParts.length < 8) {
            respondWithError(resp, "Incomplete path parameters.");
            return;
        }

        String resortID = pathParts[1];
        String seasonID = pathParts[3];
        String dayID = pathParts[5];
        String skierID = pathParts[7];

        if (!validatePathParameters(resortID, seasonID, dayID, skierID)) {
            respondWithError(resp, "Invalid path parameters.");
            return;
        }

        LiftRide liftRide = gson.fromJson(req.getReader(), LiftRide.class);

        if (!validateLiftRide(liftRide)) {
            respondWithError(resp, "Invalid LiftRide data.");
            return;
        }

        Channel channel = null;
        String queueName = "my_queue";
        String message = String.join("#", resortID, seasonID, dayID, skierID, String.valueOf(liftRide.liftID()));
        try {
            channel = channelPool.borrowChannel();
            channel.queueDeclare(queueName, true, false, false, null);
            channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("successfully publish message...");
        } catch (InterruptedException e) {
            throw new ServletException("Failed to borrow a channel from the pool", e);
        } finally {
            if(channel != null){
                channelPool.returnChannel(channel);
            }
        }

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(new ResponseMessage("Lift ride data stored successfully.")));
    }

    private boolean validatePathParameters(String resortID, String seasonID, String dayID, String skierID) {
        try {
            Integer.parseInt(resortID);
            Integer.parseInt(skierID);
            Integer.parseInt(seasonID);
            int dayIdInt = Integer.parseInt(dayID);

            if (dayIdInt < 1 || dayIdInt > DAYID_UPPER_BOUND) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }


    private boolean validateLiftRide(LiftRide liftRide) {
        return liftRide != null;
    }


    private void respondWithError(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(new ResponseMessage(message)));
    }
}
