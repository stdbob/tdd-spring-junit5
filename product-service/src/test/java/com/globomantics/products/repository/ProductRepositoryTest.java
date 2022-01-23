package com.globomantics.products.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.DBUnitExtension;
import com.globomantics.products.model.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@ExtendWith({DBUnitExtension.class, SpringExtension.class})
@SpringBootTest
public class ProductRepositoryTest {

  @TestConfiguration
  public static class ProductRepositoryTestConfiguration {
    @Bean
    public DataSource dataSource() {

      // Setup a data source for our tests
      DriverManagerDataSource dataSource = new DriverManagerDataSource();
      dataSource.setDriverClassName("org.h2.Driver");
      dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
      dataSource.setUsername("sa");
      dataSource.setPassword("");

      return dataSource;
    }
  }
  @Autowired
  private DataSource dataSource;

  @Autowired
  private ProductRepository repository;

  public ConnectionHolder getConnectionHolder() {
    // Return a function that retrieves a connection from our data source
    return () -> dataSource.getConnection();
  }

  @Test
  @DataSet("products.yml")
  void testFindAll() {
    List<Product> products = repository.findAll();
    Assertions.assertEquals(2, products.size(), "We should have 2 products in our database");
  }

  @Test
  @DataSet("products.yml")
  void testFindByIdSuccess() {
    // Find the product with ID 2
    Optional<Product> product = repository.findById(2);

    // Validate that we found it
    Assertions.assertTrue(product.isPresent(), "Product with ID 2 should be found");

    // Validate the product values
    Product p = product.get();
    Assertions.assertEquals(2, p.getId().intValue(), "Product ID should be 2");
    Assertions.assertEquals("Product 2", p.getName(), "Product name should be \"Product 2\"");
    Assertions.assertEquals(5, p.getQuantity().intValue(), "Product quantity should be 5");
    Assertions.assertEquals(2, p.getVersion().intValue(), "Product version should be 2");
  }

  @Test
  @DataSet("products.yml")
  void testFindByIdNotFound() {
    // Find the product with ID 2
    Optional<Product> product = repository.findById(3);

    // Validate that we found it
    Assertions.assertFalse(product.isPresent(), "Product with ID 3 should be not be found");
  }

  @Test
  @DataSet(value = "products.yml")
  void testSave() {
    // Create a new product and save it to the database
    Product product = Product.builder().name("Product 5").quantity(5).build();
    product.setVersion(1);
    Product savedProduct = repository.save(product);

    // Validate the saved product
    Assertions.assertEquals("Product 5", savedProduct.getName());
    Assertions.assertEquals(5, savedProduct.getQuantity().intValue());

    // Validate that we can get it back out of the database
    Optional<Product> loadedProduct = repository.findById(savedProduct.getId());
    Assertions.assertTrue(loadedProduct.isPresent(), "Could not reload product from the database");
    Assertions.assertEquals("Product 5", loadedProduct.get().getName(), "Product name does not match");
    Assertions.assertEquals(5, loadedProduct.get().getQuantity().intValue(), "Product quantity does not match");
    Assertions.assertEquals(1, loadedProduct.get().getVersion().intValue(), "Product version is incorrect");
  }

  @Test
  @DataSet(value = "products.yml")
  void testUpdateSuccess() {
    // Update product 1's name, quantity, and version
    Product product = Product.builder().id(1).name("This is product 1").quantity(100).version(5).build();
    boolean result  = repository.update(product);

    // Validate that our product is returned by update()
    Assertions.assertTrue(result, "The product should have been updated");

    // Retrieve product 1 from the database and validate its fields
    Optional<Product> loadedProduct = repository.findById(1);
    Assertions.assertTrue(loadedProduct.isPresent(), "Updated product should exist in the database");
    Assertions.assertEquals("This is product 1", loadedProduct.get().getName(), "The product name does not match");
    Assertions.assertEquals(100, loadedProduct.get().getQuantity().intValue(), "The quantity should now be 100");
    Assertions.assertEquals(5, loadedProduct.get().getVersion().intValue(), "The version should now be 5");
  }

  @Test
  @DataSet(value = "products.yml")
  void testUpdateFailure() {
    // Update product 1's name, quantity, and version
    Product product = Product.builder().id(3).name("This is product 3").quantity(100).version(5).build();
    boolean result = repository.update(product);

    // Validate that our product is returned by update()
    Assertions.assertFalse(result, "The product should not have been updated");
  }

  @Test
  @DataSet("products.yml")
  void testDeleteSuccess() {
    boolean result = repository.delete(1);
    Assertions.assertTrue(result, "Delete should return true on success");

    // Validate that the product has been deleted
    Optional<Product> product = repository.findById(1);
    Assertions.assertFalse(product.isPresent(), "Product with ID 1 should have been deleted");
  }

  @Test
  @DataSet("products.yml")
  void testDeleteFailure() {
    boolean result = repository.delete(3);
    Assertions.assertFalse(result, "Delete should return false because the deletion failed");
  }
}