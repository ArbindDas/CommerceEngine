package com.JSR.product_service.repository;

import com.JSR.product_service.entites.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends MongoRepository<Product , String> {
}
