package com.globomantics.products.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globomantics.products.model.Product;
import com.globomantics.products.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ProductService service;

  @Test
  @DisplayName("GET /product/1 - Found")
  void testGetProductByIdFound() throws Exception {
    // Setup our mocked service
    Product mockProduct =  Product.builder().id(1).name("Product Name").quantity(10).version(1).build();
    doReturn(Optional.of(mockProduct)).when(service).findById(1);

    // Execute the GET request
    mockMvc.perform(get(ProductController.REQUEST_MAPPING +"/{id}", mockProduct.getId()))

        // Validate the response code and content type
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

        // Validate the headers
        .andExpect(header().string(HttpHeaders.ETAG, String.format("\"%d\"",mockProduct.getId())))
        .andExpect(header().string(HttpHeaders.LOCATION, String.format("%s/%d", ProductController.REQUEST_MAPPING, mockProduct.getId())))

        // Validate the returned fields
        .andExpect(jsonPath("$.id", is(mockProduct.getId())))
        .andExpect(jsonPath("$.name", is(mockProduct.getName())))
        .andExpect(jsonPath("$.quantity", is(mockProduct.getQuantity())))
        .andExpect(jsonPath("$.version", is(mockProduct.getVersion())));
  }

  @Test
  @DisplayName("GET /product/1 - Not Found")
  void testGetProductByIdNotFound() throws Exception {
    // Setup our mocked service
    doReturn(Optional.empty()).when(service).findById(1);

    // Execute the GET request
    mockMvc.perform(get("/products/{id}", 1))

        // Validate that we get a 404 Not Found response
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /product/1 - Not Modified")
  void testGetProductByIdNotModified() throws Exception {
    // Setup our mocked service
    Product mockProduct =  Product.builder().id(1).name("Product Name").quantity(10).version(1).build();
    doReturn(Optional.of(mockProduct)).when(service).findById(1);

    // Execute the GET request
    mockMvc.perform(get(ProductController.REQUEST_MAPPING+ "/{id}", 1)
            .header(HttpHeaders.IF_NONE_MATCH, 1))

        // Validate that we get a 304 Not Modified response
        .andExpect(status().isNotModified());
  }

  @Test
  @DisplayName("POST /product - Success")
  void testCreateProduct() throws Exception {
    // Setup mocked service
    Product postProduct = Product.builder().name("Product Name").quantity(10).build();
    Product mockProduct = Product.builder().id(1).name("Product Name").quantity(10).version(1).build();
    doReturn(mockProduct).when(service).save(any());

    mockMvc.perform(post(ProductController.REQUEST_MAPPING)
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(postProduct)))

        // Validate the response code and content type
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

        // Validate the headers
        .andExpect(header().string(HttpHeaders.ETAG, String.format("\"%d\"",mockProduct.getId())))
        .andExpect(header().string(HttpHeaders.LOCATION, String.format("%s/%d", ProductController.REQUEST_MAPPING, mockProduct.getId())))

        // Validate the returned fields
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is("Product Name")))
        .andExpect(jsonPath("$.quantity", is(10)))
        .andExpect(jsonPath("$.version", is(1)));
  }

  @Test
  @DisplayName("PUT /product/1 - Success")
  void testProductPutSuccess() throws Exception {
    // Setup mocked service
    Product putProduct = Product.builder().name("Product Name").quantity(10).build();
    Product mockProduct = Product.builder().id(1).name("Product Name").quantity(10).version(1).build();
    doReturn(Optional.of(mockProduct)).when(service).findById(1);
    doReturn(true).when(service).update(any());

    mockMvc.perform(put(ProductController.REQUEST_MAPPING + "/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.IF_MATCH, 1)
            .content(asJsonString(putProduct)))

        // Validate the response code and content type
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

        // Validate the headers
        .andExpect(header().string(HttpHeaders.ETAG, "\"2\""))
        .andExpect(header().string(HttpHeaders.LOCATION, "/products/1"))

        // Validate the returned fields
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is("Product Name")))
        .andExpect(jsonPath("$.quantity", is(10)))
        .andExpect(jsonPath("$.version", is(2)));
  }

  @Test
  @DisplayName("PUT /product/1 - Version Mismatch")
  void testProductPutVersionMismatch() throws Exception {
    // Setup mocked service
    Product putProduct = Product.builder().name("Product Name").quantity(10).build();
    Product mockProduct = Product.builder().id(1).name("Product Name").quantity(10).version(2).build();
    doReturn(Optional.of(mockProduct)).when(service).findById(1);
    doReturn(true).when(service).update(any());

    mockMvc.perform(put(ProductController.REQUEST_MAPPING + "/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.IF_MATCH, 1)
            .content(asJsonString(putProduct)))

        // Validate the response code and content type
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("PUT /product/1 - Not Found")
  void testProductPutNotFound() throws Exception {
    // Setup mocked service
    Product putProduct = Product.builder().name("Product Name").quantity(10).build();
    doReturn(Optional.empty()).when(service).findById(1);

    mockMvc.perform(put(ProductController.REQUEST_MAPPING+ "/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.IF_MATCH, 1)
            .content(asJsonString(putProduct)))

        // Validate the response code and content type
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /product/1 - Success")
  void testProductDeleteSuccess() throws Exception {
    // Setup mocked product
    Product mockProduct =  Product.builder().id(1).name("Product Name").quantity(10).version(1).build();;

    // Setup the mocked service
    doReturn(Optional.of(mockProduct)).when(service).findById(1);
    doReturn(true).when(service).delete(1);

    // Execute our DELETE request
    mockMvc.perform(delete(ProductController.REQUEST_MAPPING + "/{id}", 1))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("DELETE /product/1 - Not Found")
  void testProductDeleteNotFound() throws Exception {
    // Setup the mocked service
    doReturn(Optional.empty()).when(service).findById(1);

    // Execute our DELETE request
    mockMvc.perform(delete(ProductController.REQUEST_MAPPING + "/{id}", 1))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /product/1 - Failure")
  void testProductDeleteFailure() throws Exception {
    // Setup mocked product
    Product mockProduct =  Product.builder().id(1).name("Product Name").quantity(10).version(1).build();

    // Setup the mocked service
    doReturn(Optional.of(mockProduct)).when(service).findById(1);
    doReturn(false).when(service).delete(1);

    // Execute our DELETE request
    mockMvc.perform(delete(ProductController.REQUEST_MAPPING + "/{id}", 1))
        .andExpect(status().isInternalServerError());
  }

  static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}