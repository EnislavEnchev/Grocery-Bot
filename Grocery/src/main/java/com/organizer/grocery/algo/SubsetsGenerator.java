package com.organizer.grocery.algo;

import com.organizer.grocery.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubsetsGenerator {

    public static List<Product> getOptimalSubset(List<List<Product>> productsToUpdate, Map<String, Integer> productNameToQuantity) {
        List<List<List<Product>>> allValidSubsets = new ArrayList<>();
        for (int i = 0; i < productNameToQuantity.size(); i++) {
            String itemRequest = productsToUpdate.get(i).get(0).getName();
            List<Product> availableProductsForItem = productsToUpdate.get(i);
            int requiredQuantity = productNameToQuantity.get(itemRequest);

            List<List<Product>> validSubsets = findSubsetsWithSum(availableProductsForItem, requiredQuantity);
            System.out.println("Valid subsets for item " + itemRequest + ": " +
                    validSubsets.stream().map(products -> products.stream().map(Product::getLocation).toList()).toList());
            allValidSubsets.add(validSubsets);
        }
        List<List<Product>> potentialProducts = new ArrayList<>();
        potentialProducts.add(new ArrayList<>());

        for (List<List<Product>> validSubsetsForItem : allValidSubsets) {
            List<List<Product>> tempCombinations = new ArrayList<>();
            for (List<Product> existingCombination : potentialProducts) {
                for (List<Product> subset : validSubsetsForItem) {
                    List<Product> newCombination = new ArrayList<>(existingCombination);
                    newCombination.addAll(subset);
                    tempCombinations.add(newCombination);
                }
            }
            potentialProducts = tempCombinations;
        }

        System.out.println("Potential product combinations: " + potentialProducts.stream()
                .map(products -> products.stream().map(Product::getLocation).toList())
                .toList());

        int optimalSubset = -1;
        int minPathLength = Integer.MAX_VALUE;
        for( List<Product> subset : potentialProducts) {
            List<Coordinate> locations = new ArrayList<>(subset.stream()
                    .map(Product::getLocation)
                    .toList());
            locations.add(new Coordinate(0, 0));
            int currentPathLength = 0;
            try{
                currentPathLength = HelperFunctions.calculateTotalDistance(Christofides.calculateOrderRoute(locations));
            } catch (IllegalArgumentException e) {
                System.err.println("Error calculating path length: " + e.getMessage());
                currentPathLength = Integer.MAX_VALUE;
            }
            System.out.println("Path length : " + currentPathLength);
            if (currentPathLength < minPathLength) {
                minPathLength = currentPathLength;
                optimalSubset = potentialProducts.indexOf(subset);
            }
        }
        return potentialProducts.get(optimalSubset);
    }
    private static List<List<Product>> findSubsetsWithSum(List<Product> products, int targetSum) {
        List<List<Product>> result = new ArrayList<>();
        findSubsetsRecursive(products, targetSum, 0, new ArrayList<>(), 0, result);
        return result;
    }

    private static void findSubsetsRecursive(List<Product> products, int targetSum, int startIndex,
                                             List<Product> currentSubset, int currentSum, List<List<Product>> result) {
        if (currentSum >= targetSum) {
            result.add(new ArrayList<>(currentSubset));
            return;
        }

        if (startIndex == products.size()) {
            return;
        }

        for (int i = startIndex; i < products.size(); i++) {
            Product currentProduct = products.get(i);
            currentSubset.add(currentProduct);
            findSubsetsRecursive(products, targetSum, i + 1, currentSubset,
                    currentSum + currentProduct.getQuantity(), result);
            currentSubset.remove(currentSubset.size() - 1); // Backtrack
        }
    }
}
