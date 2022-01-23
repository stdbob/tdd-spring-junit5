package com.globomantics.products.service;

import com.globomantics.products.model.Product;
import com.globomantics.products.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * Tests the ProductService.
 */
@SpringBootTest(classes = ProductServiceImpl.class)
class ProductServiceTest {

  /**
   * The service that we want to test.
   */
  @Autowired
  private ProductService service;

  /**
   * A mock version of the ProductRepository for use in our tests.
   */
  @MockBean
  private ProductRepository repository;

  @Test
  @DisplayName("Test findById Success")
  void testFindByIdSuccess() {
    // Setup our mock
    Product mockProduct = Product.builder().id(1).name("Product Name").quantity(10).version(1).build();
    doReturn(Optional.of(mockProduct)).when(repository).findById(1);

    // Execute the service call
    Optional<Product> returnedProduct = service.findById(1);

    // Assert the response
    Assertions.assertTrue(returnedProduct.isPresent(), "Product was not found");
    Assertions.assertSame(returnedProduct.get(), mockProduct, "Products should be the same");
  }

  @Test
  @DisplayName("Test findById Not Found")
  void testFindByIdNotFound() {
    // Setup our mock
    Product mockProduct = Product.builder().id(1).name("Product Name").quantity(10).version(1).build();
    doReturn(Optional.empty()).when(repository).findById(1);

    // Execute the service call
    Optional<Product> returnedProduct = service.findById(1);

    // Assert the response
    Assertions.assertFalse(returnedProduct.isPresent(), "Product was found, when it shouldn't be");
  }

  @Test
  @DisplayName("Test findAll")
  void testFindAll() {
    // Setup our mock
    Product mockProduct = Product.builder().id(1).name("Product Name").quantity(10).version(1).build();
    Product mockProduct2 = Product.builder().id(2).name("Product Name 2").quantity(15).version(3).build();;
    doReturn(Arrays.asList(mockProduct, mockProduct2)).when(repository).findAll();

    // Execute the service call
    List<Product> products = service.findAll();

    Assertions.assertEquals(2, products.size(), "findAll should return 2 products");
  }

  @Test
  @DisplayName("Test save product")
  void testSave() {
    Product mockProduct = Product.builder().id(1).name("Product Name").quantity(10).build();;
    doReturn(mockProduct).when(repository).save(any());

    Product returnedProduct = service.save(mockProduct);

    Assertions.assertNotNull(returnedProduct, "The saved product should not be null");
    Assertions.assertEquals(1, returnedProduct.getVersion().intValue(),
        "The version for a new product should be 1");
  }
}