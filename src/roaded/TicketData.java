package roaded;

// Data from JSON object is stored in our TicketData class
// to be processed by RoadEd
public class TicketData {
    private long ticket_id;
    // inside location
    private Double loc_lat;
    private Double loc_long;
    private String issue_type;
    // neighborhood_district
    private String ward;
    private int calendar_year;
    private String ticket_created_date_time;
    private String ticket_closed_date_time;
    private String ticket_status;

    public void setTicket_id(String ticket_id) {
        this.ticket_id = Long.parseLong(ticket_id.toString());
    }

    public long getTicket_id() {
        return ticket_id;
    }

    public void setLoc_lat(Double latitude) {
        this.loc_lat = latitude;
    }

    public void setLoc_long(Double longitude) {
        this.loc_long = longitude;
    }
    
    public Double getLoc_lat() {
        return loc_lat;
    }
    
    public Double getLoc_long() {
        return loc_long;
    }

    public void setIssue_type(String issue_type) {
        this.issue_type = issue_type;
    }
    
    public String getIssueType() {
        return issue_type;
    }

    public void setWard(String neighbor_dist) {
        this.ward = neighbor_dist;
    }

    public String getWard() {
        return ward;
    }

    public void setCalendar_year(int year) {
        this.calendar_year = year;
    }

    public void setTicket_created_date_time(String created_date_time) {
        this.ticket_created_date_time = created_date_time;
    }

    public void setTicket_closed_date_time(String closed_date_time) {
        if (closed_date_time == null) {
            return;
        }
        this.ticket_closed_date_time = closed_date_time;
    }

    public void setTicket_status(String ticket_status) {
        if (ticket_status == null) {
            return;
        }
        this.ticket_status = ticket_status;
    }

    public String getTicket_status() {
        return ticket_status;
    }

    @Override
    public String toString() {
        return this.getTicket_status();
    }
}