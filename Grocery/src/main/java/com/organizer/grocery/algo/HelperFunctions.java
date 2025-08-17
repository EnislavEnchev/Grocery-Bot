package com.organizer.grocery.algo;

import com.organizer.grocery.model.Coordinate;
import com.organizer.grocery.model.Product;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

public class HelperFunctions {
    public static int calculateDistance(Coordinate p1, Coordinate p2) {
        return Math.abs(p1.x() - p2.x()) + Math.abs(p1.y() - p2.y());
    }

    public static int calculateTotalDistance(List<Coordinate> coordinates) {
        int distance = 0;
        for (int i = 1; i < coordinates.size(); i++) {
            Coordinate prev = coordinates.get(i - 1);
            Coordinate curr = coordinates.get(i);
            distance += Math.abs(curr.x() - prev.x()) + Math.abs(curr.y() - prev.y());
        }
        return distance;
    }

    public static int calculateDistance(Product p1, Product p2) {
        return Math.abs(p1.getLocation().x() - p2.getLocation().x()) + Math.abs(p1.getLocation().y() - p2.getLocation().y());
    }

    public static int calculateTotalProductDistance(List<Product> coordinates) {
        int distance = 0;
        for (int i = 1; i < coordinates.size(); i++) {
            Coordinate prev = coordinates.get(i - 1).getLocation();
            Coordinate curr = coordinates.get(i).getLocation();
            distance += Math.abs(curr.x() - prev.x()) + Math.abs(curr.y() - prev.y());
        }
        return distance;
    }

    public static String getUsernameFromContext(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
