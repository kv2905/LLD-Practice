/*
Design a parking lot
*/

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


public class IdGenerator {
    public static String generateUniqueId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}

public enum VEHICLE_TYPE {
    MOTOR_CYCLE,
    CAR,
    TRUCK
}

public enum PARKING_TYPE {
    SMALL,
    MEDIUM,
    LARGE
}

public class Vehicle {
    private String vehicleID;
    private VEHICLE_TYPE type;

    public Vehicle(VEHICLE_TYPE type) {
        this.type = type;
        this.vehicleID = IdGenerator.generateUniqueId();
    }

    public VEHICLE_TYPE getType() {
        return this.type;
    }

    public String getID() {
        return this.vehicleID;
    }
}

public class ParkingSpot {
    private PARKING_TYPE type;
    private boolean isAvailble;
    private Vehicle vehicle;

    public ParkingSpot(PARKING_TYPE type) {
        this.type = type;
        this.isAvailble = true;
        this.vehicle = null;
    }

    public boolean checkIfAvailable() {
        return this.isAvailble;
    }

    public PARKING_TYPE getType() {
        return this.type;
    }

    public Vehicle getVehicle() {
        return this.vehicle;
    }

    public void parkVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.isAvailble = false;
    }

    public void freeSpot() {
        this.vehicle = null;
        this.isAvailble = true;
    }
}

public class Floor {
    private List<ParkingSpot> parkingSpots;

    public Floor(List<ParkingSpot> parkingSpots) {
        this.parkingSpots = parkingSpots;
    }

    public List<ParkingSpot> getFreeParkingSpots() {
        return this.parkingSpots.stream()
                   . filter(spot -> spot.checkIfAvailable())
                   .collect(Collectors.toList());
    }

    public List<ParkingSpot> getAllSpots() {
        return this.parkingSpots;
    }
}

public class Ticket {
    private String ticketID;
    private Vehicle vehicle;
    private LocalDateTime entryTime;

    public Ticket(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.entryTime = LocalDateTime.now();
        this.ticketID = IdGenerator.generateUniqueId();
    }

    public Vehicle getVehicle() {
        return this.vehicle;
    }

    public String getID() {
        return this.ticketID;
    }

    public LocalDateTime getEntryTime() {
        return this.entryTime;
    }

}

public class TicketGenerator {
    public static Ticket generateTicket(Vehicle vehicle) {
        return new Ticket(vehicle);
    }
}

public class ParkingManager {
    private List<Floor> parkingFloors;

    public ParkingManager(List<Floor> parkingFloors) {
        this.parkingFloors = parkingFloors;
    }

    public ParkingSpot getNextAvailableParkingSpotForVehicle(Vehicle vehicle) {
        switch(vehicle.getType()) {
            case MOTOR_CYCLE:
                for(Floor floor: parkingFloors) {
                    for(ParkingSpot parkingSpot: floor.getFreeParkingSpots()) {
                        if (parkingSpot.getType() == PARKING_TYPE.SMALL) {
                            return parkingSpot;
                        }
                    }
                }

                return null;
            
            case CAR:
                for(Floor floor: parkingFloors) {
                    for(ParkingSpot parkingSpot: floor.getFreeParkingSpots()) {
                        if (parkingSpot.getType() == PARKING_TYPE.MEDIUM) {
                            return parkingSpot;
                        }
                    }
                }

                return null;
            
            case TRUCK:
                for(Floor floor: parkingFloors) {
                    for(ParkingSpot parkingSpot: floor.getFreeParkingSpots()) {
                        if (parkingSpot.getType() == PARKING_TYPE.LARGE) {
                            return parkingSpot;
                        }
                    }
                }

                return null;
            
            default:
                return null;
        }
    }

    public void freeParkingSpot(Vehicle vehicle) {
        for(Floor floor: this.parkingFloors) {
            for(ParkingSpot spot: floor.getAllSpots()) {
                Vehicle parkedVehicle = spot.getVehicle();
                if (parkedVehicle != null && parkedVehicle.getID().equals(vehicle.getID())) {
                    spot.freeSpot();

                    return;
                }
            }
        }
    }
}

public class TicketManager {
    public static List<Ticket> tickets = new ArrayList<>();

    public static void addTicket(Ticket ticket) {
        TicketManager.tickets.add(ticket);
    }
}

public class EntryGate {
    private ParkingManager parkingManager;

    public EntryGate(ParkingManager parkingManager) {
        this.parkingManager = parkingManager;
    }

    public boolean permitParking(Vehicle vehicle) {
        ParkingSpot availableParkingSpot = this.parkingManager.getNextAvailableParkingSpotForVehicle(vehicle);

        if (availableParkingSpot == null) {
            return false;
        }

        availableParkingSpot.parkVehicle(vehicle);
        TicketManager.addTicket(TicketGenerator.generateTicket(vehicle));
        return true;
    }
}

public class PaymentManager {
    private static final int HOURLY_RATE = 20;

    public static int generatePayment(LocalDateTime entryTime, LocalDateTime exitTime) {
        long durationInHours = Duration.between(entryTime, exitTime).toHours();
        
        // Minimum charge for at least 1 hour
        if (durationInHours == 0) {
            durationInHours = 1;
        }

        return (int) durationInHours * HOURLY_RATE;
    }
}

public class ExitGate {
    private ParkingManager parkingManager;

    public ExitGate(ParkingManager parkingManager) {
        this.parkingManager = parkingManager;
    }

    public void freeParkingSpot(Ticket vehicleTicket) {
        for (Ticket ticket: TicketManager.tickets) {
            if (vehicleTicket.getID().equals(ticket.getID())) {
                int payableAmount = PaymentManager.generatePayment(vehicleTicket.getEntryTime(), LocalDateTime.now());
                this.parkingManager.freeParkingSpot(vehicleTicket.getVehicle());
            }
        }
    }
}