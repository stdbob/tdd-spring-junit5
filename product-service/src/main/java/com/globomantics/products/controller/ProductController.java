package com.globomantics.products.controller;

import com.globomantics.products.model.Product;
import com.globomantics.products.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(ProductController.REQUEST_MAPPING)
public class ProductController {

  public static final String REQUEST_MAPPING = "/products";

  private final ProductService productService;


  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  /**
   * Returns the product with the specified ID.
   *
   * @param id The ID of the product to retrieve.
   * @return The product with the specified ID.
   */
  @GetMapping("/{id}")
  public ResponseEntity<?> getProduct(@PathVariable Integer id, @RequestHeader(value = "If-None-Match", required = false) Integer ifNoneMatch) {

    return productService.findById(id)
        .map(product -> {
          try {
            if (product.getVersion().equals(ifNoneMatch)) {
              return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }

            return ResponseEntity
                .ok()
                .eTag(Integer.toString(product.getVersion()))
                .location(new URI(ProductController.REQUEST_MAPPING +"/" + product.getId()))
                .body(product);
          } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
          }
        })
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Returns all products in the database.
   *
   * @return All products in the database.
   */
  @GetMapping
  public Iterable<Product> getProducts() {
    return productService.findAll();
  }

  /**
   * Creates a new product.
   *
   * @param product The product to create.
   * @return The created product.
   */
  @PostMapping
  public ResponseEntity<Product> createProduct(@RequestBody Product product) {
    log.info("Creating new product with name: {}, quantity: {}", product.getName(), product.getQuantity());

    // Create the new product
    Product newProduct = productService.save(product);

    try {
      // Build a created response
      return ResponseEntity
          .created(new URI(ProductController.REQUEST_MAPPING +"/" + newProduct.getId()))//TODO UriComponentsBuilder
          .eTag(Integer.toString(newProduct.getVersion()))
          .body(newProduct);
    } catch (URISyntaxException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Updates the fields in the specified product with the specified ID.
   *
   * @param product The product field values to update.
   * @param id      The ID of the product to update.
   * @param ifMatch The eTag version of the product.
   * @return A ResponseEntity that contains the updated product or one of the following error statuses:
   * NOT_FOUND if there is no product in the database with the specified ID
   * CONFLICT if the eTag does not match the version of the product to update
   * INTERNAL_SERVICE_ERROR if there is a problem creating the location URI
   */
  @PutMapping("/{id}")
  public ResponseEntity<?> updateProduct(@RequestBody Product product,
                                         @PathVariable Integer id,
                                         @RequestHeader("If-Match") Integer ifMatch) {
    log.info("Updating product with id: {}, name: {}, quantity: {}",
        id, product.getName(), product.getQuantity());

    // Get the existing product
    Optional<Product> existingProduct = productService.findById(id);

    return existingProduct.map(p -> {
      // Compare the etags
      log.info("Product with ID: " + id + " has a version of " + p.getVersion()
          + ". Update is for If-Match: " + ifMatch);
      if (!p.getVersion().equals(ifMatch)) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
      }

      // Update the product
      p.setName(product.getName());
      p.setQuantity(product.getQuantity());
      p.setVersion(p.getVersion() + 1);

      log.info("Updating product with ID: " + p.getId()
          + " -> name=" + p.getName()
          + ", quantity=" + p.getQuantity()
          + ", version=" + p.getVersion());

      try {
        // Update the product and return an ok response
        if (productService.update(p)) {
          return ResponseEntity.ok()
              .location(new URI(ProductController.REQUEST_MAPPING +"/" + p.getId()))
              .eTag(Integer.toString(p.getVersion()))
              .body(p);
        } else {
          return ResponseEntity.notFound().build();
        }
      } catch (URISyntaxException e) {
        // An error occurred trying to create the location URI, return an error
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }

    }).orElse(ResponseEntity.notFound().build());
  }

  /**
   * Deletes the product with the specified ID.
   *
   * @param id The ID of the product to delete.
   * @return A ResponseEntity with one of the following status codes:
   * 200 OK if the delete was successful
   * 404 Not Found if a product with the specified ID is not found
   * 500 Internal Service Error if an error occurs during deletion
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {

    log.info("Deleting product with ID {}", id);

    // Get the existing product
    Optional<Product> existingProduct = productService.findById(id);

    return existingProduct.map(p -> {
      if (productService.delete(p.getId())) {
        return ResponseEntity.ok().build();
      } else {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
    }).orElse(ResponseEntity.notFound().build());
  }
}
