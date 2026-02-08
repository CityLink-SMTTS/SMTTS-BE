package com.nullsquad.CityLink.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "route_name", length = 100)
    private String routeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type")
    private VehicleType vehicleType;

    @Column(name = "start_terminal")
    private Long startTerminal;

    @Column(name = "end_terminal")
    private Long endTerminal;

    public enum VehicleType {
        BUS,
        TRAIN,
        TAXI
    }
}
