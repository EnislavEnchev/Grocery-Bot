package com.organizer.grocery.service;


import com.organizer.grocery.algo.RouteStrategy;
import com.organizer.grocery.model.*;
import com.organizer.grocery.dto.RouteDto;
import com.organizer.grocery.repository.ProductRepository;
import com.organizer.grocery.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RouteServiceImplementation implements RouteService {

    private final RouteRepository routeRepository;
    private final RouteStrategy routeStrategy;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final CacheService cacheService;

    @Autowired
    public RouteServiceImplementation(RouteRepository routeRepository, @Qualifier("ChristofidesStrategy") RouteStrategy routeStrategy,
                                      ProductService productService, ProductRepository productRepository, CacheService cacheService) {
        this.routeRepository = routeRepository;
        this.routeStrategy = routeStrategy;
        this.productService = productService;
        this.productRepository = productRepository;
        this.cacheService = cacheService;
    }

    @Override
    @Transactional
    public void saveRouteForOrder(Order order, Map<String, Integer> productNameToQuantity, List<Product> chosenProducts) throws Exception {
        List<PickedProduct> fullPath = new ArrayList<>();
        for (Product product : chosenProducts) {
            int totalQuantity = productNameToQuantity.getOrDefault(product.getName(), 0);
            int usedQuantity = Math.min(product.getQuantity(), totalQuantity);
            int remainingQuantity = totalQuantity - usedQuantity;
            fullPath.add(new PickedProduct(product, usedQuantity));
            product.setQuantity(Math.max(product.getQuantity() - usedQuantity, 0));
            productService.notifyUpdateProduct(product);
            productNameToQuantity.put(product.getName(), remainingQuantity);
            if (product.getQuantity() == 0) {
                productService.deleteProduct(product.getId(), true);
            } else {
                productRepository.save(product);
            }
        }

        Route route = new Route(null, order, fullPath);
        routeRepository.save(route);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "routes", key = "#orderId")
    public RouteDto getRouteByOrderId(Long orderId) {
        Optional<Route> route = routeRepository.findByOrderId(orderId);
        if( route.isEmpty() ) {
            return new RouteDto(
                    orderId,
                    OrderStatus.PENDING,
                    List.of()
            );
        }

        return new RouteDto(
                route.get().getOrder().getId(),
                route.get().getOrder().getStatus(),
                route.get().getVisitedLocations()
        );
    }

}
