package com.JSR.product_service.service.impl;

import com.JSR.product_service.dto.ProductRequest;
import com.JSR.product_service.dto.ProductResponse;
import com.JSR.product_service.entites.Product;
import com.JSR.product_service.repository.ProductRepository;
import com.JSR.product_service.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class ProductServiceImpl implements ProductService {


    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponse add(ProductRequest request) {
        Product product = Product.builder()
                        .name(request.name())
                        .description(request.description())
                        .price(request.price())
                . build();

        Product savedProduct = productRepository.save(product);
        log.info("Product is {} saved ", product.getId());
//        return new ProductResponse(
//                save.getId(),
//                save.getName(),
//                save.getDescription(),
//                save.getPrice()
//        );
       return mapToProductResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
//        return productRepository.findAll()
//                .stream()
//                .map(product ->  new ProductResponse(
//                        product.getId(),
//                        product.getName(),
//                        product.getDescription(),
//                        product.getPrice()
//                ))
//                .toList();
        return productRepository.findAll()
                .stream().map(this::mapToProductResponse).toList();

    }

    @Override
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id : " + id));
        return mapToProductResponse(product);
    }

    @Override
    public void deleteProductById(String id) {
        productRepository.deleteById(id);
    }

    @Override
    public ProductResponse updateProductById(String id, ProductRequest request) {


        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id : "+id));


        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());

        Product updatedProduct = productRepository.save(product);
        return mapToProductResponse(updatedProduct);
//        return new ProductResponse(
//                updatedProduct.getId(),
//                updatedProduct.getName(),
//                updatedProduct.getDescription(),
//                updatedProduct.getPrice()
//        );
    }

    @Override
    public ProductResponse updateProductByIdPatch(String id, ProductRequest request) {


        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with : "+id));



//        if (request.name()!=null && !request.name().isEmpty()){
//            product.setName(request.name());
//        }
//
//
//        if (request.description()!=null && !request.description().isEmpty()){
//            product.setDescription(request.description());
//        }
//
//        if (request.price()!=null){
//            product.setPrice(request.price());
//        }

        validateChecker(product , request);


        Product updatedProduct = productRepository.save(product);

//        return new ProductResponse(
//                updatedProduct.getId(),
//                updatedProduct.getName(),
//                updatedProduct.getDescription(),
//                updatedProduct.getPrice()
//        );

        return mapToProductResponse(updatedProduct);


    }

    private ProductResponse mapToProductResponse(Product request){
      return  new ProductResponse(
              request.getId(),
              request.getName(),
              request.getDescription(),
              request.getPrice()
      );
    }

    private void validateChecker( Product product ,  ProductRequest request){

        if (request.name()!=null && !request.name().isEmpty()){
            product.setName(request.name());
        }

        if (request.description()!=null && !request.description().isEmpty()){
            product.setDescription(request.description());
        }

        if (request.price()!=null){
            product.setPrice(request.price());
        }
    }
}
