/*
Design a parking lot
*/

// ========= Imports ========= //

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

// ========= Enums ========= //

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

// ========= Utility Classes ========= //

public class IdGenerator {
    public static String generateUniqueId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}

public class TicketGenerator {
    public static Ticket generateTicket(Vehicle vehicle) {
        return new Ticket(vehicle);
    }
}

// ========= Core Domain Models ========= //

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
                   .filter(spot -> spot.checkIfAvailable())
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

public class ExitGate {
    private ParkingManager parkingManager;
    private PricingStrategy pricingStrategy;

    public ExitGate(ParkingManager parkingManager, PricingStrategy pricingStrategy) {
        this.parkingManager = parkingManager;
        this.pricingStrategy = pricingStrategy;
    }

    public int freeParkingSpot(Ticket vehicleTicket) {
        for (Ticket ticket: TicketManager.tickets) {
            if (vehicleTicket.getID().equals(ticket.getID())) {
                int payableAmount = this.pricingStrategy.generateParkingPrice(vehicleTicket.getEntryTime(), LocalDateTime.now());
                this.parkingManager.freeParkingSpot(vehicleTicket.getVehicle());

                return payableAmount;
            }
        }

        return 0;
    }
}

// ========= Helpers ========== //

interface SpotAssignmentStrategy {
    ParkingSpot assignSpot(List<Floor> floors, Vehicle vehicle);
}

public class DefaultSpotAssignmentStrategy implements SpotAssignmentStrategy {
    public ParkingSpot assignSpot(List<Floor> floors, Vehicle vehicle) {
        PARKING_TYPE requiredType;
        switch (vehicle.getType()) {
            case MOTOR_CYCLE: requiredType = PARKING_TYPE.SMALL; break;
            case CAR: requiredType = PARKING_TYPE.MEDIUM; break;
            case TRUCK: requiredType = PARKING_TYPE.LARGE; break;
            default: return null;
        }

        for (Floor floor : floors) {
            for (ParkingSpot spot : floor.getFreeParkingSpots()) {
                if (spot.getType() == requiredType) {
                    return spot;
                }
            }
        }
        return null;
    }
}

interface PricingStrategy {
    int generateParkingPrice(LocalDateTime entryTime, LocalDateTime exitTime);
}

public class DefaultPricingStrategy implements PricingStrategy {
    private final int HOURLY_RATE = 20;

    public int generateParkingPrice(LocalDateTime entryTime, LocalDateTime exitTime) {
        long durationInHours = Duration.between(entryTime, exitTime).toHours();
        
        // Minimum charge for at least 1 hour
        if (durationInHours == 0) {
            durationInHours = 1;
        }

        return (int) durationInHours * HOURLY_RATE;
    }
}

// ========= Managers ========= //

public class ParkingManager {
    private List<Floor> parkingFloors;
    private SpotAssignmentStrategy spotAssignmentStrategy;

    public ParkingManager(List<Floor> parkingFloors, SpotAssignmentStrategy spotAssignmentStrategy) {
        this.parkingFloors = parkingFloors;
        this.spotAssignmentStrategy = spotAssignmentStrategy;
    }

    public ParkingSpot getNextAvailableParkingSpotForVehicle(Vehicle vehicle) {
        return spotAssignmentStrategy.assignSpot(this.parkingFloors, vehicle);
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

// ========= Main (or Test) ========= //
public class ParkingLotMain {
    public static void main(String[] args) {
        List<ParkingSpot> spotsFloor1 = Arrays.asList(
            new ParkingSpot(PARKING_TYPE.SMALL),
            new ParkingSpot(PARKING_TYPE.MEDIUM),
            new ParkingSpot(PARKING_TYPE.LARGE)
        );

        Floor floor1 = new Floor(spotsFloor1);
        List<Floor> floors = Arrays.asList(floor1);

        SpotAssignmentStrategy strategy = new DefaultSpotAssignmentStrategy();
        ParkingManager parkingManager = new ParkingManager(floors, strategy);

        EntryGate entryGate = new EntryGate(parkingManager);
        Vehicle bike = new Vehicle(VEHICLE_TYPE.MOTOR_CYCLE);
        Vehicle car = new Vehicle(VEHICLE_TYPE.CAR);

        System.out.println("Bike allowed: " + entryGate.permitParking(bike));
        System.out.println("Car allowed: " + entryGate.permitParking(car));

        for (ParkingSpot spot : floor1.getAllSpots()) {
            System.out.println(spot.getType() + " | Available: " + spot.checkIfAvailable());
        }
    }
}
