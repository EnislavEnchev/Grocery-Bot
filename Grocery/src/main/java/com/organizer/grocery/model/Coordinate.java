package com.organizer.grocery.model;

import jakarta.persistence.Embeddable;

@Embeddable
public record Coordinate(int x, int y) {
}

