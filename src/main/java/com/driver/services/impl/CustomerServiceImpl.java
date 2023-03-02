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
		//Get The Customer
		Customer customer = customerRepository2.findById(customerId).get();
		//1. Get the available driver
		List<Driver> drivers = driverRepository2.findAll();
		List<Integer> driverIds = new ArrayList<>();
		for(Driver d : drivers)
		{
			driverIds.add(d.getDriverId());
		}
		Collections.sort(driverIds);
		//Searching for available driver with lowest driverId
		boolean availableDriverId = false;
		//2. Get the driver if available
		Driver driver = new Driver();
		for(Integer driverId : driverIds)
		{
			if(driverRepository2.findById(driverId).isPresent()) {
				driver = driverRepository2.findById(driverId).get();
				if (driver.getCab().getAvailable()) {
					availableDriverId = true;
					break;
				}
			}
		}
		if(!availableDriverId)
		{
			throw new Exception("No cab available!");
		}
		//Driver is available so,(Driver Found)
		//3. Get the cab
		//Update Cab's attribute
//		Cab cab = driver.getCab();
//		cab.setAvailable(false);
		driver.getCab().setAvailable(false);
		//Set the trip
		TripBooking tripBooking = new TripBooking();
		//Set attributes then save
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setCustomer(customerRepository2.findById(customerId).get());
		tripBooking.setDriver(driver);
		tripBooking.setBill(driver.getCab().getPerKmRate()*distanceInKm);

		//Add trip in customer's tripBookingList
//		List<TripBooking> customerTrips = customer.getTripBookingList();
		customer.getTripBookingList().add(tripBooking);
		customerRepository2.save(customer);
//		customer.setTripBookingList(customerTrips);
		//Add trip in driver's tripBookingList(Not Required)
//		List<TripBooking> driverTrips = driver.getTripBookingList();
		driver.getTripBookingList().add(tripBooking);
//		driver.setTripBookingList(driverTrips);
		//Saving
		driverRepository2.save(driver);
		//TripBooking will be saved twice!! by  cascading effect

		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		if(tripBooking.getStatus() == TripStatus.CONFIRMED) {
			tripBooking.setStatus(TripStatus.CANCELED);
			tripBooking.getDriver().getCab().setAvailable(true);
		}
		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		//Get trip object
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		if(tripBooking.getStatus() == TripStatus.CONFIRMED) {
			tripBooking.setStatus(TripStatus.COMPLETED);
			tripBooking.getDriver().getCab().setAvailable(true);
		}
		tripBookingRepository2.save(tripBooking);
	}
}
