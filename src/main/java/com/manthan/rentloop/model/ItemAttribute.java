package com.manthan.rentloop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "item_attributes")
@Getter
@Setter
@NoArgsConstructor
public class ItemAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "attribute_key", nullable = false, length = 100)
    private String attributeKey; // e.g., "RAM", "Brand", "Color"

    @Column(name = "attribute_value", nullable = false, length = 255)
    private String attributeValue; // e.g., "16GB", "Yamaha", "Red"
}