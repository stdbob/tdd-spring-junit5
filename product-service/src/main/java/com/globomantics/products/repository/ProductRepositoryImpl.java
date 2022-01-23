package com.globomantics.products.repository;

import com.globomantics.products.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class ProductRepositoryImpl implements ProductRepository {

  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert simpleJdbcInsert;

  public ProductRepositoryImpl(JdbcTemplate jdbcTemplate, DataSource dataSource) {
    this.jdbcTemplate = jdbcTemplate;

    // Build a SimpleJdbcInsert object from the specified data source
    this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)//TODO get DataSource from jdbcTemplate?
        .withTableName("products")
        .usingGeneratedKeyColumns("id");
  }

  @Override
  public Optional<Product> findById(Integer id) {
    try {
      Product product = jdbcTemplate.queryForObject("SELECT * FROM products WHERE id = ?",
          (rs, rowNum) ->
             Product.builder()
              .id(rs.getInt("id"))
              .name(rs.getString("name"))
              .quantity(rs.getInt("quantity"))
              .version(rs.getInt("version")).build()
          , new Object[]{id});
      return Optional.of(product);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  @Override
  public List<Product> findAll() {
    return jdbcTemplate.query("SELECT * FROM products",
        (rs, rowNumber) -> Product.builder()
            .id(rs.getInt("id"))
            .name(rs.getString("name"))
            .quantity(rs.getInt("quantity"))
            .version(rs.getInt("version")).build());
  }

  @Override
  public boolean update(Product product) {
    return jdbcTemplate.update("UPDATE products SET name = ?, quantity = ?, version = ? WHERE id = ?",
        product.getName(),
        product.getQuantity(),
        product.getVersion(),
        product.getId()) == 1;
  }

  @Override
  public Product save(Product product) {
    // Build the product parameters we want to save
    Map<String, Object> parameters = new HashMap<>(1);
    parameters.put("name", product.getName());
    parameters.put("quantity", product.getQuantity());
    parameters.put("version", product.getVersion());

    // Execute the query and get the generated key
    Number newId = simpleJdbcInsert.executeAndReturnKey(parameters);

    log.info("Inserting product into database, generated key is: {}", newId);

    // Update the product's ID with the new key
    product.setId((Integer)newId);

    // Return the complete product
    return product;
  }

  @Override
  public boolean delete(Integer id) {
    return jdbcTemplate.update("DELETE FROM products WHERE id = ?", id) == 1;
  }
}
