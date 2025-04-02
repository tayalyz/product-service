package com.microservices.product_service;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {

	@ServiceConnection
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");

	@LocalServerPort
	private Integer port;

	@Test
	void contextLoads() {
	}

	@BeforeEach
	void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	static {
		mongoDBContainer.start();
	}

	@Test
	void shouldCreateProduct() {
		String requestBody = """
				{
				    "name": "name1",
				    "description": "desc1",
				    "price": 100
				}
				""";
		RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/product")
				.then()
				.statusCode(201)
				.body("id", Matchers.notNullValue())
				.body("name", Matchers.equalTo("name1"))
				.body("description", Matchers.equalTo("desc1"))
				.body("price", Matchers.equalTo(100));

		String requestBody2 = """
				{
				    "name": "name2",
				    "description": "desc2",
				    "price": 1000
				}
				""";
		RestAssured.given()
				.contentType("application/json")
				.body(requestBody2)
				.when()
				.post("/api/product")
				.then()
				.statusCode(201)
				.body("id", Matchers.notNullValue())
				.body("name", Matchers.equalTo("name2"))
				.body("description", Matchers.equalTo("desc2"))
				.body("price", Matchers.equalTo(1000));
	}

	@Test
	void shouldGetAllProducts() {
		RestAssured.given()
				.contentType("application/json")
				.when()
				.get("/api/product")
				.peek()
				.then()
				.statusCode(200)
				.body("id", Matchers.notNullValue())
				.body("name", hasItems("name2", "name1"))
				.body("description", hasItems("desc1", "desc2"))
				.body("price", hasItems(100, 1000))
				.body("size()", is(2));
	}
}
