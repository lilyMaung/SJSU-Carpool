import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CarpoolServer {
    static final List<Trip> trips = new ArrayList<>();
    static final List<RideRequest> requests = new ArrayList<>();
    static int nextTrip=1, nextRequest=1;

    public static void main(String[] args) throws Exception {
        HttpServer s=HttpServer.create(new InetSocketAddress(8080),0);
        s.createContext("/", e -> send(e, home()));
        s.createContext("/driver", e -> send(e, driverPage()));
        s.createContext("/rider", e -> send(e, riderPage()));
        s.createContext("/post-trip", e -> { postTrip(form(e)); redirect(e,"/driver"); });
        s.createContext("/request-ride", e -> { requestRide(form(e)); redirect(e,"/rider"); });
        s.createContext("/accept", e -> { changeStatus(form(e),"ACCEPTED"); redirect(e,"/driver"); });
        s.createContext("/reject", e -> { changeStatus(form(e),"REJECTED"); redirect(e,"/driver"); });
        s.createContext("/cancel", e -> { changeStatus(form(e),"CANCELED"); redirect(e,"/rider"); });
        s.createContext("/driver-cancel", e -> { changeStatus(form(e),"CANCELED"); redirect(e,"/driver"); });
        s.createContext("/pickup-status", e -> { updatePickupStatus(form(e)); redirect(e,"/driver"); });
        s.createContext("/message", e -> {
            Map<String,String> p=form(e); addMessage(p);
            redirect(e, p.getOrDefault("side","rider").equals("driver")?"/driver":"/rider");
        });
        s.start();
        System.out.println("Open http://localhost:8080");
    }

    static void postTrip(Map<String,String> p) {
        trips.add(new Trip(nextTrip++, p.get("studentId"), p.get("from"), p.get("to"),
            p.get("date"), p.get("time"), Integer.parseInt(p.get("seats")),
            p.get("vehicle"), p.get("color"), p.get("plate")));
    }

    static void requestRide(Map<String,String> p) {
        double offer=0;
        try { if(!p.getOrDefault("offer","").isBlank()) offer=Double.parseDouble(p.get("offer")); } catch(Exception ignored){}
        requests.add(new RideRequest(nextRequest++, Integer.parseInt(p.get("tripId")),
            p.get("studentId"), p.get("pickup"), offer));
    }

    static void changeStatus(Map<String,String> p,String status) {
        int id=Integer.parseInt(p.get("id"));
        for(RideRequest r:requests) {
            if(r.id==id) {
                if(status.equals("ACCEPTED")) {
                    Trip trip=null;
                    for(Trip t:trips) if(t.id==r.tripId) trip=t;
                    if(trip!=null && seatsLeft(trip)>0) r.status="ACCEPTED";
                } else {
                    r.status=status;
                }
            }
        }
    }

    static String home() {
        return top("SJSU Carpool") + """
        <div class="center card login">
          <div class="logo">SJSU<br><span>CARPOOL</span></div>
          <h2>Welcome</h2>
          <p>For SJSU students only</p>
          <label>Demo Student ID</label>
          <input id="sid" placeholder="Example: 012345678">
          <p class="note">For this prototype, use a fake Student ID.</p>
          <div class="role">
            <button onclick="go('/driver')">🚗 I'm a Driver</button>
            <button class="light" onclick="go('/rider')">👤 I'm a Rider</button>
          </div>
        </div>
        <script>
        function go(path){
          if(!document.getElementById('sid').value.trim()){alert('Enter a demo Student ID first.');return;}
          sessionStorage.setItem('sid',document.getElementById('sid').value.trim());
          location.href=path;
        }
        </script>""" + bottom();
    }

    static String driverPage() {
        StringBuilder myTrips=new StringBuilder();
        if(trips.isEmpty()) myTrips.append("<div class='empty'>You have not posted a trip yet.</div>");
        for(Trip t:trips) {
            myTrips.append("<div class='card trip'><div class='badge'>ACTIVE</div><h3>")
              .append(h(t.from)).append(" → ").append(h(t.to)).append("</h3><p>")
              .append(h(t.date)).append(" • ").append(h(t.time)).append(" • ")
              .append(seatsLeft(t)).append(" seats available</p><p class='small'>")
              .append(h(t.color)).append(" ").append(h(t.vehicle)).append("</p>");

            boolean any=false;
            for(RideRequest r:requests) if(r.tripId==t.id) {
                if(!any){myTrips.append("<h4>Ride Requests</h4>"); any=true;}
                myTrips.append("<div class='request'><b>Student ").append(h(r.riderId))
                  .append("</b><br>📍 <a target='_blank' href='").append(appleMapsLink(r.pickupAddress)).append("'>")
                  .append(h(r.pickupAddress)).append("</a>")
                  .append("<br><a class='mapbtn' target='_blank' href='").append(appleMapsLink(r.pickupAddress))
                  .append("'>Open in Apple Maps</a>")
                  .append("<br>Offer: $").append(String.format("%.0f",r.offer))
                  .append("<br><b>").append(r.status).append("</b>");
                if(r.status.equals("ACCEPTED")) {
                    myTrips.append("<div class='accepted'><b>Pickup status: ").append(h(r.pickupStatus)).append("</b>")
                      .append("<div class='actions'>")
                      .append(statusAction(r.id,"ON MY WAY","On My Way"))
                      .append(statusAction(r.id,"I'M HERE","I'm Here"))
                      .append(statusAction(r.id,"PICKED UP","Picked Up"))
                      .append("</div>")
                      .append(messagesHtml(r))
                      .append("<form action='/message' method='post'><input type='hidden' name='id' value='").append(r.id)
                      .append("'><input type='hidden' name='side' value='driver'><input name='text' placeholder='Message rider...' required><button>Send</button></form>")
                      .append("<form action='/driver-cancel' method='post'><input type='hidden' name='id' value='").append(r.id)
                      .append("'><button class='red'>Cancel This Ride</button></form></div>");
                }
                if(r.status.equals("PENDING")) {
                    myTrips.append("<div class='actions'>")
                      .append(action("/accept",r.id,"Accept",""))
                      .append(action("/reject",r.id,"Reject","red"))
                      .append("</div>");
                }
                myTrips.append("</div>");
            }
            myTrips.append("</div>");
        }

        return top("Driver") + """
        <div class="nav"><a href="/">← Home</a><b>Driver Dashboard</b></div>
        <div class="layout">
        <div class="card">
          <h2>🚗 Post a Trip</h2>
          <form action="/post-trip" method="post" onsubmit="return addSid(this)">
            <input type="hidden" name="studentId">
            <label>From</label><input name="from" placeholder="Hayward, CA" required>
            <label>To</label><input name="to" placeholder="San José State University" required>
            <div class="two"><div><label>Date</label><input type="date" name="date" required></div>
            <div><label>Leave at</label><input type="time" name="time" required></div></div>
            <label>Available Seats</label><input type="number" name="seats" min="1" max="6" value="3" required>
            <h3>Vehicle Information</h3>
            <label>Make / Model</label><input name="vehicle" placeholder="Toyota Corolla" required>
            <div class="two"><div><label>Color</label><input name="color" placeholder="White" required></div>
            <div><label>Plate Number</label><input name="plate" placeholder="ABC1234" required></div></div>
            <button>Post Trip</button>
          </form>
        </div>
        <div><h2>My Trips</h2>""" + myTrips + """
        </div></div>
        <script>
        function addSid(f){f.studentId.value=sessionStorage.getItem('sid')||'DEMO';return true;}
        </script>""" + bottom();
    }

    static String riderPage() {
        StringBuilder cards=new StringBuilder();
        if(trips.isEmpty()) cards.append("<div class='empty'>No rides are available yet. A driver needs to post a trip first.</div>");
        for(Trip t:trips) {
            cards.append("<div class='card trip'><h3>").append(h(t.from)).append(" → ").append(h(t.to))
              .append("</h3><p>").append(h(t.date)).append(" • ").append(h(t.time))
              .append("</p><p class='green'>").append(seatsLeft(t)).append(seatsLeft(t)==1 ? " seat available</p>" : " seats available</p>")
              ;
            if(seatsLeft(t)>0) {
                cards.append("<details><summary>Request this ride</summary>")
                  .append("<form action='/request-ride' method='post' onsubmit='return addSid(this)'>")
                  .append("<input type='hidden' name='studentId'><input type='hidden' name='tripId' value='").append(t.id).append("'>")
                  .append("<label>Exact Pickup Address</label><input name='pickup' placeholder='Example: 123 Main St, Fremont, CA' required>")
                  .append("<label>Offer to Driver (optional)</label><input type='number' min='0' name='offer' placeholder='10'>")
                  .append("<button>Send Request</button></form></details>");
            } else {
                cards.append("<p class='redtext'><b>FULL — No seats available</b></p>");
            }

            for(RideRequest r:requests) if(r.tripId==t.id && r.status.equals("ACCEPTED")) {
                cards.append("<div class='accepted'><b>✓ Accepted Ride</b><br>")
                  .append("Driver vehicle: ").append(h(t.color)).append(" ").append(h(t.vehicle))
                  .append("<br>Plate: ").append(h(t.plate))
                  .append("<br>Pickup: ").append(h(r.pickupAddress))
                  .append("<br><b>Driver status: ").append(h(r.pickupStatus)).append("</b>")
                  .append(messagesHtml(r))
                  .append("<form action='/message' method='post'><input type='hidden' name='id' value='").append(r.id)
                  .append("'><input type='hidden' name='side' value='rider'><input name='text' placeholder='Message driver...' required><button>Send</button></form>")
                  .append("<form action='/cancel' method='post'><input type='hidden' name='id' value='").append(r.id)
                  .append("'><button class='red'>Cancel Ride</button></form></div>");
            }
            for(RideRequest r:requests) if(r.tripId==t.id && r.status.equals("CANCELED")) {
                cards.append("<div class='canceled'><b>Ride canceled</b> • Pickup: ")
                     .append(h(r.pickupAddress)).append("</div>");
            }
            cards.append("</div>");
        }

        return top("Rider") + """
        <div class="nav"><a href="/">← Home</a><b>Available Rides</b></div>
        <div class="narrow"><h2>👤 Find a Ride</h2>
        <p class="note">Choose a trip, enter your exact pickup address, and send the request.</p>
        """ + cards + """
        </div>
        <script>
        function addSid(f){f.studentId.value=sessionStorage.getItem('sid')||'DEMO';return true;}
        </script>""" + bottom();
    }


    static int acceptedCount(int tripId) {
        int count=0;
        for(RideRequest r:requests)
            if(r.tripId==tripId && r.status.equals("ACCEPTED")) count++;
        return count;
    }

    static int seatsLeft(Trip t) {
        return Math.max(0, t.seats - acceptedCount(t.id));
    }

    static void updatePickupStatus(Map<String,String> p) {
        int id=Integer.parseInt(p.get("id"));
        String value=p.getOrDefault("value","WAITING");
        for(RideRequest r:requests)
            if(r.id==id && r.status.equals("ACCEPTED")) r.pickupStatus=value;
    }

    static void addMessage(Map<String,String> p) {
        int id=Integer.parseInt(p.get("id"));
        String text=p.getOrDefault("text","").trim();
        String side=p.getOrDefault("side","rider");
        if(text.isEmpty()) return;
        for(RideRequest r:requests) if(r.id==id && r.status.equals("ACCEPTED")) {
            String who=side.equals("driver") ? "Driver" : "Rider";
            if(!r.messages.isEmpty()) r.messages += "\\n";
            r.messages += who + ": " + text;
        }
    }

    static String messagesHtml(RideRequest r) {
        if(r.messages==null || r.messages.isBlank()) return "<p class='small'>No messages yet.</p>";
        StringBuilder b=new StringBuilder("<div class='messages'>");
        for(String line:r.messages.split("\\\\n")) b.append("<div>").append(h(line)).append("</div>");
        return b.append("</div>").toString();
    }

    static String appleMapsLink(String address) {
        try {
            return "https://maps.apple.com/?daddr=" +
                URLEncoder.encode(address, StandardCharsets.UTF_8);
        } catch(Exception e) {
            return "https://maps.apple.com/";
        }
    }

    static String statusAction(int id,String value,String label) {
        return "<form action='/pickup-status' method='post'><input type='hidden' name='id' value='"+id+
            "'><input type='hidden' name='value' value='"+h(value)+"'><button>"+label+"</button></form>";
    }

    static String action(String url,int id,String text,String cls) {
        return "<form action='"+url+"' method='post'><input type='hidden' name='id' value='"+id+"'><button class='"+cls+"'>"+text+"</button></form>";
    }

    static String top(String title) {
        return "<!doctype html><html><head><meta charset='utf-8'>"
        +"<meta name='viewport' content='width=device-width,initial-scale=1'>"
        +"<title>"+title+"</title><style>"
        +"*{box-sizing:border-box}body{margin:0;background:#f4f7fb;color:#1e293b;font-family:Arial,sans-serif}"
        +"header{background:#075aa8;color:white;padding:18px 7%;font-size:23px;font-weight:bold}"
        +".nav{max-width:1050px;margin:20px auto;display:flex;justify-content:space-between;padding:0 18px}"
        +"a{color:#075aa8;text-decoration:none}.center{max-width:440px;margin:55px auto}.login{text-align:center}"
        +".logo{font-size:38px;font-weight:bold;color:#075aa8;line-height:.9}.logo span{font-size:20px}"
        +".card{background:white;border-radius:16px;padding:22px;box-shadow:0 4px 18px #dfe5ec;margin-bottom:18px}"
        +".layout{max-width:1050px;margin:auto;padding:0 18px;display:grid;grid-template-columns:1fr 1fr;gap:25px}"
        +".narrow{max-width:720px;margin:auto;padding:0 18px}.two{display:grid;grid-template-columns:1fr 1fr;gap:12px}"
        +"label{display:block;font-weight:bold;font-size:13px;margin:12px 0 5px}"
        +"input{width:100%;padding:12px;border:1px solid #c5ccd5;border-radius:8px;font-size:15px}"
        +"button{background:#075aa8;color:white;border:0;border-radius:8px;padding:12px 18px;font-size:15px;cursor:pointer}"
        +"button.light{background:white;color:#075aa8;border:1px solid #075aa8}.role{display:grid;gap:10px;margin-top:18px}"
        +".note,.small{color:#64748b}.green{color:#16834b;font-weight:bold}.red{background:#d83b3b}"
        +".badge{float:right;background:#dff6e7;color:#16834b;padding:5px 8px;border-radius:7px;font-size:12px}"
        +".request{background:#f5f7fa;padding:13px;border-radius:10px;margin-top:10px;line-height:1.7}"
        +".actions{display:flex;gap:8px;margin-top:8px}.actions form{margin:0}.empty{background:white;padding:25px;border-radius:14px;color:#64748b}"
        +"details{margin-top:14px}summary{color:#075aa8;font-weight:bold;cursor:pointer}"
        +".redtext{color:#d83b3b}.accepted{margin-top:15px;background:#e9f8ef;border-radius:10px;padding:14px;line-height:1.6}"
        +".messages{background:white;border:1px solid #dfe5ec;border-radius:8px;padding:9px;margin:10px 0;line-height:1.7}.canceled{margin-top:12px;background:#fff1f1;color:#a52a2a;padding:12px;border-radius:9px}.mapbtn{display:inline-block;margin:8px 0;padding:8px 12px;background:#eef5ff;border-radius:7px;font-weight:bold}"+"@media(max-width:760px){.layout,.two{grid-template-columns:1fr}.center{margin:25px 18px}}"
        +"</style></head><body><header>SJSU CARPOOL</header>";
    }

    static String bottom(){return "</body></html>";}
    static String h(String s){if(s==null)return "";return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");}

    static Map<String,String> form(HttpExchange e)throws IOException {
        String b=new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);
        Map<String,String> m=new HashMap<>();
        for(String pair:b.split("&")){
            String[] a=pair.split("=",2);
            m.put(URLDecoder.decode(a[0],StandardCharsets.UTF_8),
                  a.length>1?URLDecoder.decode(a[1],StandardCharsets.UTF_8):"");
        }
        return m;
    }
    static void redirect(HttpExchange e,String path)throws IOException {
        e.getResponseHeaders().add("Location",path);e.sendResponseHeaders(303,-1);e.close();
    }
    static void send(HttpExchange e,String s)throws IOException {
        byte[] b=s.getBytes(StandardCharsets.UTF_8);
        e.getResponseHeaders().set("Content-Type","text/html; charset=utf-8");
        e.sendResponseHeaders(200,b.length);e.getResponseBody().write(b);e.close();
    }
}