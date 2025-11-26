package com.JSR.order_service.entites;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@Table(name = "t_order")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String orderName;

    @OneToMany(mappedBy = "order" , cascade = CascadeType.ALL , orphanRemoval = true)
    private List<OrderLineItems> orderLineItemsList;
}
