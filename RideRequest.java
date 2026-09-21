public class RideRequest {
    public int id, tripId;
    public String riderId, pickupAddress, status="PENDING";
    public String pickupStatus="WAITING";
    public String messages="";
    public double offer;

    public RideRequest(int id, int tripId, String riderId, String pickupAddress, double offer) {
        this.id=id; this.tripId=tripId; this.riderId=riderId;
        this.pickupAddress=pickupAddress; this.offer=offer;
    }
}