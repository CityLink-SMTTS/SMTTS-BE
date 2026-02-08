package com.nullsquad.CityLink.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "vehicle_number", unique = true, length = 50)
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "terminal_id")
    private Long terminalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VehicleStatus status = VehicleStatus.ACTIVE;

    public enum VehicleType {
        BUS,
        TRAIN,
        TAXI
    }

    public enum VehicleStatus {
        ACTIVE,
        INACTIVE,
        MAINTENANCE
    }
}
