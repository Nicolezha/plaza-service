package com.pragma.powerup.infrastructure.out.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "restaurants")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 20, unique = true, nullable = false)
    private String nit;

    @Column(nullable = false)
    private String address;

    @Column(length = 13, nullable = false)
    private String phone;

    @Column(nullable = false)
    private String logoUrl;

    @Column(nullable = false)
    private Long ownerId;
}
