package com.nullsquad.CityLink.service;

import com.nullsquad.CityLink.entity.Trip;
import com.nullsquad.CityLink.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TripService {

    @Autowired
    private TripRepository tripRepository;

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }
}
