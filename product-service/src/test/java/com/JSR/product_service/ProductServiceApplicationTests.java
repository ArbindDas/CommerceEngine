package com.JSR.product_service;

import com.JSR.product_service.dto.ProductRequest;
import com.JSR.product_service.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Disabled
@SpringBootTest // Starts the complete Spring application context for integration testing
@Testcontainers // Enables Testcontainers support for managing Docker containers in tests
@AutoConfigureMockMvc // Automatically configures MockMvc for testing web endpoints
@Slf4j // Provides logger instance via Lombok
@DirtiesContext // Resets Spring application context after each test class to avoid state pollution
class ProductServiceApplicationTests {

    @Container // Marks this as a Testcontainers container that will be managed automatically
    static MongoDBContainer mongoDBContainer = new MongoDBContainer( "mongo:8.0.16" ); // Creates MongoDB Docker container

    @Autowired // Injects MockMvc instance for making HTTP requests in tests
    private MockMvc mockMvc;

    @Autowired // Injects ProductRepository to interact with database directly
    private ProductRepository productRepository;

    @Autowired // Injects ObjectMapper for JSON serialization/deserialization
    private ObjectMapper objectMapper;

    @DynamicPropertySource // Dynamically sets properties before Spring context starts
    static void setProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        // Overrides MongoDB connection URI to use Testcontainers instance instead of application.properties
        registry.add( "spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl );
    }

    @Test
    void shouldCreateProdouct() throws Exception {
        // Create test data
        ProductRequest request = getProductRequest();
        // Convert object to JSON string for HTTP request body
        String productRequest = objectMapper.writeValueAsString( request );

        // Perform POST request to create product
        mockMvc.perform( MockMvcRequestBuilders.post( "/api/product" )
                        .contentType( MediaType.APPLICATION_JSON ) // Set content type as JSON
                        .content( productRequest ) ) // Set request body
                .andExpect( status().isCreated() ); // Verify HTTP 201 status

        // Retrieve all products from database to verify creation
        var products = productRepository.findAll();

        // Log products for debugging
        log.info("Products in Testcontainers MongoDB:");
        products.forEach(System.out::println);

        // Assert that exactly one product was created
        Assertions.assertEquals(1, products.size());
    }

    private ProductRequest getProductRequest() {
        // Helper method to create consistent test data
        return new ProductRequest(
                "apple iphone 8", // Product name
                "efficient mobile", // Product description
                new BigDecimal( 56000 ) // Product price
        );
    }

    @AfterEach // Runs after each test method
    void cleanup() {
        // Cleans up database to ensure test isolation
        // Prevents tests from affecting each other
        productRepository.deleteAll();
    }

    @Test
    void getAllProducts() throws Exception {
        // Given - create test data
        ProductRequest request = getProductRequest();
        String productRequest = objectMapper.writeValueAsString(request);

        // Create a product via POST request
        mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest))
                .andExpect(status().isCreated());

        // When - call get all products endpoint and verify
        log.info("the getAll method working.....");

        // Perform GET request and capture result for debugging
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/product"))
                .andExpect(status().isOk()) // Verify HTTP 200 status
                .andExpect(jsonPath("$.length()").value(1)) // Verify JSON array has exactly 1 element
                .andReturn(); // Capture the result to inspect response content

        // Log the actual response content for debugging
        String responseContent = result.getResponse().getContentAsString();
        log.info("GET Response: {}", responseContent);

        // Also check what's in the database directly
        var productsInDb = productRepository.findAll();
        log.info("Products in database: {}", productsInDb.size());
        productsInDb.forEach(p -> log.info("DB Product: {}", p));
    }
}