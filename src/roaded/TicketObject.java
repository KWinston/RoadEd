package roaded;

import com.google.gson.*;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

public class TicketObject {

    private ArrayList<TicketData> ticketData = new ArrayList<>();

    public static String storeJSON() throws IOException {
        String url = "https://data.edmonton.ca/resource/ukww-xkmj.json";
        BufferedReader input = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));

        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = input.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

    public ArrayList<TicketData> getData() {
        return ticketData;
    }

    public int grabWard(String number) {
        int counter = 0;
        for (TicketData entry : ticketData) {
            if (Integer.parseInt(number) < 10 && entry.getWard() != null && entry.getWard().contains("WARD 0" + number)) {
                counter++;
            }
            else if (Integer.parseInt(number) > 9 && entry.getWard() != null && entry.getWard().contains("WARD " + number)) {
                counter++;
            }
        }
        return counter;
    }

    public void parse(String jsonLine) throws IOException {
        JsonParser jp = new JsonParser();
        JsonElement jsonElement = jp.parse(storeJSON());
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject row = jsonArray.get(i).getAsJsonObject();
         
            //Create new ticket data object
            TicketData ticketObj = new TicketData();
            ticketObj.setTicket_id(row.get("ticket_id").getAsString());
            ticketObj.setLoc_lat(row.getAsJsonObject("location").getAsJsonObject().get("latitude").getAsDouble());
            ticketObj.setLoc_long(row.getAsJsonObject("location").getAsJsonObject().get("longitude").getAsDouble());
            ticketObj.setIssue_type(row.get("issue_type").getAsString());
            if (row.get("neighborhood_district") != null) {
                ticketObj.setWard(row.get("neighborhood_district").getAsString());
            }
            ticketObj.setCalendar_year(row.get("calendar_year").getAsInt());
            ticketObj.setTicket_created_date_time(row.get("ticket_created_date_time").getAsString());
            if (row.get("ticket_closed_date_time") != null) {
                ticketObj.setTicket_closed_date_time(row.get("ticket_closed_date_time").getAsString());
            }
            if (row.get("ticket_status") != null) {
                ticketObj.setTicket_status(row.get("ticket_status").getAsString());
            }
            ticketData.add(ticketObj);
        }
    }
}