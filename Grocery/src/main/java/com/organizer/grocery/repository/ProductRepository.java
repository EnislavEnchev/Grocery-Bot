package com.organizer.grocery.repository;

import com.organizer.grocery.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByLocation_XAndLocation_Y(int x, int y);
    Optional<Product> findByName(String name);
    //@Query("SELECT p FROM Product p WHERE p.name = :name")
    //List<Product> findAllByName(String name);
}