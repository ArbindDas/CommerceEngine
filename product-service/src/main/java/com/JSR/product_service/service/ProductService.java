package com.JSR.product_service.service;


import com.JSR.product_service.dto.ProductRequest;
import com.JSR.product_service.dto.ProductResponse;

import java.util.List;

public interface ProductService {



    ProductResponse add(ProductRequest request);
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(String id);
    void deleteProductById(String id);
    ProductResponse updateProductById(String id , ProductRequest request);

    ProductResponse updateProductByIdPatch(String id , ProductRequest request);

}
