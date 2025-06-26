package com.organizer.grocery.service;


import com.organizer.grocery.model.Order;
import com.organizer.grocery.model.Coordinate;
import com.organizer.grocery.model.Product;
import com.organizer.grocery.dto.RouteDto;
import com.organizer.grocery.model.Route;
import com.organizer.grocery.repository.RouteRepository;
import com.organizer.grocery.repository.OrderRepository;
import com.organizer.grocery.algo.Christofides;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class RouteServiceImplementation implements RouteService {

    private final RouteRepository routeRepository;
    private final OrderRepository orderRepository;

    public RouteServiceImplementation(RouteRepository routeRepository, OrderRepository orderRepository) {
        this.routeRepository = routeRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void createAndSaveRouteForOrder(Order order) {
        Order fullOrder = orderRepository.findByIdWithOrderItemsAndProducts(order.getId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + order.getId()));

        List<Coordinate> locationsToVisit = fullOrder.getOrderItems().stream()
                .map(orderItem -> orderItem.getProduct().getLocation())
                .distinct()
                .collect(Collectors.toList());

        locationsToVisit.add(new Coordinate(0, 0));
        List<Coordinate> path = Christofides.calculateOrderRoute(locationsToVisit);
        List<Coordinate> filledPath = fillGapsInPath(path);
        Route route = new Route(null, fullOrder, filledPath);
        routeRepository.save(route);
    }

    @Override
    @Transactional(readOnly = true)
    public RouteDto getRouteByOrderId(Long orderId) {
        Route route = routeRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Route not found for order id: " + orderId));

        return new RouteDto(
                route.getOrder().getId(),
                route.getOrder().getStatus(),
                route.getVisitedLocations()
        );
    }

    private List<Coordinate> fillGapsInPath(List<Coordinate> path) {
        List<Coordinate> filled = new ArrayList<>();
        if (path.isEmpty()) {
            return filled;
        }
        filled.add(path.get(0));
        for (int i = 1; i < path.size(); i++) {
            Coordinate prev = path.get(i - 1);
            Coordinate cur = path.get(i);
            int x = prev.x(), y = prev.y();
            while (x != cur.x()) {
                x += Integer.compare(cur.x(), x);
                filled.add(new Coordinate(x, y));
            }
            while (y != cur.y()) {
                y += Integer.compare(cur.y(), y);
                filled.add(new Coordinate(x, y));
            }
        }
        return filled;
    }

//    private int manhattanDistance(Coordinate p1, Coordinate p2) {
//        return Math.abs(p1.x() - p2.x()) + Math.abs(p1.y() - p2.y());
//    }
}
