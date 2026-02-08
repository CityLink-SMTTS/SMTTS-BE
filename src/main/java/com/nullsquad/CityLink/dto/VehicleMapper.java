package com.nullsquad.CityLink.dto;

import com.nullsquad.CityLink.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
    VehicleMapper INSTANCE = Mappers.getMapper(VehicleMapper.class);

    @Mapping(source = "assignedZone.id", target = "assignedZoneId")
    @Mapping(source = "assignedZone.zoneName", target = "assignedZoneName")
    @Mapping(source = "currentRoute.id", target = "currentRouteId")
    @Mapping(source = "currentRoute.routeName", target = "currentRouteName")
    VehicleDTO toDTO(Vehicle vehicle);

    @Mapping(target = "assignedZone", ignore = true)
    @Mapping(target = "currentRoute", ignore = true)
    @Mapping(target = "lastLocationUpdate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Vehicle toEntity(VehicleDTO vehicleDTO);
}
