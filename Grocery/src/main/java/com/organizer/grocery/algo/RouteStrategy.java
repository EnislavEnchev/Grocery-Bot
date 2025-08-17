package com.organizer.grocery.algo;

import com.organizer.grocery.model.Product;
import com.organizer.grocery.model.ProductsForAlgorithms;

import java.util.List;
import java.util.Map;

public interface RouteStrategy {
    List<Product> getOptimalRoute(List<List<Product>> productsToUpdate, Map<String, Integer> productNameToQuantity);
    List<Product> getOptimalRoute(ProductsForAlgorithms input);
}
