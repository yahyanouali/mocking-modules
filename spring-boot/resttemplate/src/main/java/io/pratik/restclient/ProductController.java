package io.pratik.restclient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.pratik.apis.models.Product;

@RestController
public class ProductController {
    private List<Product> products = new ArrayList<>(List.of(
            new Product("Television", "Samsung", 1145.67, "S001"),
            new Product("Washing Machine", "LG", 114.67, "L001"),
            new Product("Laptop", "Apple", 11453.67, "A001")));

    @GetMapping(value = "/products/{id}", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public Product fetchProducts(@PathVariable String id) {
        return products.get(1);
    }

    @GetMapping("/products")
    public List<Product> fetchProducts() {
        return products;
    }

    @PostMapping("/products")
    public ResponseEntity<String> createProduct(@RequestBody Product product) {
        String productID = UUID.randomUUID().toString();
        product.setId(productID);
        products.add(product);

        return ResponseEntity.ok().body("{\"productID\":\"" + productID + "\"}");
    }

    @PutMapping("/products")
    public ResponseEntity<String> updateProduct(@RequestBody Product product) {
        products.set(1, product);
        // Update product. Return success or failure without response body
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/products")
    public ResponseEntity<String> deleteProduct(@RequestBody Product product) {
        products.remove(1);
        // Update product. Return success or failure without response body
        return ResponseEntity.ok().build();
    }

    @GetMapping("/products/error")
    public ResponseEntity<Product> fetchProductWithError() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }


}
