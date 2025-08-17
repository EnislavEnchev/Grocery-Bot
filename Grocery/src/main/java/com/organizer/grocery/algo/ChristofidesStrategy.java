package com.organizer.grocery.algo;

import com.organizer.grocery.model.Product;
import com.organizer.grocery.model.ProductsForAlgorithms;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component("ChristofidesStrategy")
public class ChristofidesStrategy implements RouteStrategy{

    @Override
    public List<Product> getOptimalRoute(List<List<Product>> productsToUpdate, Map<String, Integer> productNameToQuantity) {
        return SubsetsGenerator.getOptimalSubset(productsToUpdate, productNameToQuantity);
    }

    @Override
    public List<Product> getOptimalRoute(ProductsForAlgorithms input) {
        return SubsetsGenerator.getOptimalSubset(input.productsToUpdate(), input.productNameToQuantity());
    }
}
