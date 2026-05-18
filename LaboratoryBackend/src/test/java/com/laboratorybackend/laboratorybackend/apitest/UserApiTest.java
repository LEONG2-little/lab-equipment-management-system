package com.laboratorybackend.laboratorybackend.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    @Test
    void testLogin_Success() {
        Map<String, Object> request = new HashMap<>();
        request.put("account", 2022160085);
        request.put("password", "12345678");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("status", equalTo(200))
                .body("message", equalTo("成功"));
    }

    @Test
    void testGetAllDevices_Success() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/getAllDevices")
                .then()
                .statusCode(200)
                .body("status", equalTo(200));
    }
}