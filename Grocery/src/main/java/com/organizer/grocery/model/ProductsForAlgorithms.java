package com.organizer.grocery.model;

import java.util.List;
import java.util.Map;

public record ProductsForAlgorithms(
        Map<String, Integer> productNameToQuantity, List<List<Product>> productsToUpdate
) {}
