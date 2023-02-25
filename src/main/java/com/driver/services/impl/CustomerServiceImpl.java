package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import io.swagger.models.auth.In;
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

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
//		customerRepository2.deleteCustomer(customerId);
		customerRepository2.deleteById(customerId);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		//Get The Customer
		Customer customer = customerRepository2.findById(customerId).get();
		//Get the available driver
		List<Driver> drivers = driverRepository2.findAll();
		List<Integer> driverIds = new ArrayList<>();
		for(Driver d : drivers)
		{
			driverIds.add(d.getDriverId());
		}
		Collections.sort(driverIds);
		//Searching for available driver with lowest driverId
		boolean availableDriverId = false;
		//Getting driver if available
		Driver driver = new Driver();
		for(Integer driverId : driverIds)
		{
			driver = driverRepository2.findById(driverId).get();
			if(driver.getCab().isAvailable()){
				driver.getCab().setAvailable(false);
				availableDriverId = true;
				break;
			}
		}
		if(availableDriverId)
		{
			throw new Exception("No cab available!");
		}
		//Driver is available so,(Driver Found)
		//Set the trip
		TripBooking tripBooking = new TripBooking();
		//Set attributes then save
		tripBooking.setTripStatus(TripStatus.CONFIRMED);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setCustomer(customerRepository2.findById(customerId).get());
		tripBooking.setDriver(driver);

		//Add trip in customer's tripBookingList
		List<TripBooking> customerTrips = customer.getTripBookingList();
		customerTrips.add(tripBooking);
		customer.setTripBookingList(customerTrips);
		//Add trip in driver's tripBookingList
		List<TripBooking> driverTrips = driver.getTripBookingList();
		driverTrips.add(tripBooking);
		driver.setTripBookingList(driverTrips);
		//Saving
//		customerRepository2.save(customerRepository2.findById(customerId).get());
		driverRepository2.save(driver);
		//TripBooking will be saved twice!! by  cascading effect

		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly

	}
}
