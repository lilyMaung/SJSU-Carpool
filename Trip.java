public class Trip {
    public int id;
    public String driverId, from, to, date, time, vehicle, color, plate;
    public int seats;

    public Trip(int id, String driverId, String from, String to, String date, String time,
                int seats, String vehicle, String color, String plate) {
        this.id=id; this.driverId=driverId; this.from=from; this.to=to;
        this.date=date; this.time=time; this.seats=seats;
        this.vehicle=vehicle; this.color=color; this.plate=plate;
    }
}