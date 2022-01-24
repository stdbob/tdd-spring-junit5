package com.globomantics.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globomantics.products.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductServiceIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("GET /products/1 - Found")
	void testGetProductByIdFound() throws Exception {
		// Execute the GET request
		mockMvc.perform(get("/products/{id}", 1))

				// Validate the response code and content type
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

				// Validate the headers
				.andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
				.andExpect(header().string(HttpHeaders.LOCATION, "/products/1"))

				// Validate the returned fields
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is("Product 1")))
				.andExpect(jsonPath("$.quantity", is(10)))
				.andExpect(jsonPath("$.version", is(1)));
	}

	@Test
	@DisplayName("GET /products/99 - Not Found")
	void testGetProductByIdNotFound() throws Exception {
		// Execute the GET request
		mockMvc.perform(get("/products/{id}", 99))

				// Validate that we get a 404 Not Found response
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("POST /products - Success")
	void testCreateProduct() throws Exception {
		// Setup product to create
		Product postProduct = Product.builder().name("Product Name").quantity(10).build();

		mockMvc.perform(post("/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(asJsonString(postProduct)))

				// Validate the response code and content type
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

				// Validate the headers
				.andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
				.andExpect(header().exists(HttpHeaders.LOCATION))

				// Validate the returned fields
				.andExpect(jsonPath("$.id", any(Integer.class)))
				.andExpect(jsonPath("$.name", is("Product Name")))
				.andExpect(jsonPath("$.quantity", is(10)))
				.andExpect(jsonPath("$.version", is(1)));
	}

	@Test
	@DisplayName("PUT /products/2 - Success")
	void testProductPutSuccess() throws Exception {
		// Setup product to update
		Product putProduct = Product.builder().name("Product 2 Updated").quantity(10).build();

		mockMvc.perform(put("/products/{id}", 2)
						.contentType(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.IF_MATCH, 2)
						.content(asJsonString(putProduct)))

				// Validate the response code and content type
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

				// Validate the headers
				.andExpect(header().string(HttpHeaders.ETAG, "\"3\""))
				.andExpect(header().string(HttpHeaders.LOCATION, "/products/2"))

				// Validate the returned fields
				.andExpect(jsonPath("$.id", is(2)))
				.andExpect(jsonPath("$.name", is("Product 2 Updated")))
				.andExpect(jsonPath("$.quantity", is(10)))
				.andExpect(jsonPath("$.version", is(3)));
	}

	@Test
	@DisplayName("PUT /products/1 - Version Mismatch")
	void testProductPutVersionMismatch() throws Exception {
		// Setup product to update
		Product putProduct = Product.builder().name("Product Name").quantity(10).build();

		mockMvc.perform(put("/products/{id}", 1)
						.contentType(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.IF_MATCH, 7)
						.content(asJsonString(putProduct)))

				// Validate the response code and content type
				.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("PUT /products/99 - Not Found")
	void testProductPutNotFound() throws Exception {
		// Setup product to update
		Product putProduct = Product.builder().name("Product Name").quantity(10).build();

		mockMvc.perform(put("/products/{id}", 99)
						.contentType(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.IF_MATCH, 1)
						.content(asJsonString(putProduct)))

				// Validate the response code and content type
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("DELETE /products/1 - Success")
	void testProductDeleteSuccess() throws Exception {
		// Execute our DELETE request
		mockMvc.perform(delete("/products/{id}", 1))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("DELETE /products/99 - Not Found")
	void testProductDeleteNotFound() throws Exception {
		// Execute our DELETE request
		mockMvc.perform(delete("/products/{id}", 99))
				.andExpect(status().isNotFound());
	}

	static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
