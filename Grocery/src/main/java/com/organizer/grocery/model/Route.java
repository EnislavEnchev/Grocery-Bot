package com.organizer.grocery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "", nullable = false)

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "route_locations",
            joinColumns = @JoinColumn(name = "route_id")
    )
    @OrderColumn(name = "step_order", nullable = false)
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "location_x", nullable = false)),
            @AttributeOverride(name = "y", column = @Column(name = "location_y", nullable = false)),
            @AttributeOverride(name = "name", column = @Column(name = "product_name", nullable = false)),
            @AttributeOverride(name = "quantity", column = @Column(name = "product_quantity", nullable = false)),
            @AttributeOverride(name = "price", column = @Column(name = "product_price", nullable = false))
    })
    private List<PickedProduct> visitedLocations;
}
