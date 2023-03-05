package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.CabRepository;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;
	@Autowired
	private CabRepository cabRepository;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer = customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

//		if(!customerRepository2.findById(customerId).isPresent()) {
//			return new TripBooking();
//		}

		//1. Get the available driver
		List<Driver> drivers = driverRepository2.findAll();
		Driver driver = null;

		for(Driver currentDriver : drivers) {
			if(currentDriver.getCab().getAvailable()) {
				if (driver == null || currentDriver.getDriverId() < driver.getDriverId()) {
					driver = currentDriver;
				}
			}
		}

		if(driver == null)
		{
			throw new Exception("No cab available!");
		}
		//Driver is available
		driver.getCab().setAvailable(false);


		TripBooking tripBooking = new TripBooking();

		//2. Get The Customer
		Customer customer = customerRepository2.findById(customerId).get();//getting customer before getting driver caused failed test cases(when no cab is available)
		//Set attributes then save

		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setBill(driver.getCab().getPerKmRate()*distanceInKm);
		tripBooking.setCustomer(customer);
		tripBooking.setDriver(driver);

		//Add trip in customer's tripBookingList
		customer.getTripBookingList().add(tripBooking);
		customerRepository2.save(customer);

		driver.getTripBookingList().add(tripBooking);
		driverRepository2.save(driver);
		//TripBooking will be saved by  cascading effect

		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
//		if(tripBooking.getStatus() == TripStatus.CONFIRMED) {
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);
		tripBooking.getDriver().getCab().setAvailable(Boolean.TRUE);
//		}
		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		//Get trip object
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
//		if(tripBooking.getStatus() == TripStatus.CONFIRMED) {
			tripBooking.setStatus(TripStatus.COMPLETED);
			tripBooking.getDriver().getCab().setAvailable(true);
//		}
		tripBookingRepository2.save(tripBooking);
	}
}
