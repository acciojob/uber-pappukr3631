package com.driver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.driver.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>{
    @Modifying
    @Query(value = "DELETE FROM customer WHERE customer_id=:customerId;",nativeQuery = true)
    void deleteCustomer(int customerId);
}
