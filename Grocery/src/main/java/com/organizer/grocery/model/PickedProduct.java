package com.organizer.grocery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickedProduct implements Serializable {

    public PickedProduct(Product product, int usedQuantity) {
        this.name = product.getName();
        this.quantity = usedQuantity;
        this.price = product.getPrice().doubleValue();
        this.x = product.getLocation().x();
        this.y = product.getLocation().y();
    }
    private String name;
    private int quantity;
    private double price;
    private int x;
    private int y;

}
