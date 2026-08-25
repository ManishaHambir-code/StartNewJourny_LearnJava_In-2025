package com.scp.java.ocm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OcmSecurityIT {
    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void unauthenticatedReadReturns401() {
        ResponseEntity<String> response = rest.getForEntity(url("/api/v1/members"), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void wrongPasswordReturns401WithoutDisclosingCredentialDetails() throws Exception {
        ResponseEntity<String> response =
                rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new HttpEntity<String>(
                                "{\"username\":\"admin\",\"password\":\"wrong\"}", jsonHeaders()),
                        String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(
                "Invalid username or password",
                mapper.readTree(response.getBody()).get("message").asText());
    }

    @Test
    void garbageBearerTokenReturns401() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("not-a-jwt");

        ResponseEntity<String> response =
                rest.exchange(
                        url("/api/v1/members"), HttpMethod.GET, new HttpEntity<Void>(headers), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void adminCanCreateAndReadMember() throws Exception {
        String token = login("admin", "Admin@12345");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<String>(memberJson("IT-MRN"), headers);
        ResponseEntity<String> created =
                rest.postForEntity(url("/api/v1/members"), request, String.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        Long id = mapper.readTree(created.getBody()).get("id").asLong();
        ResponseEntity<String> fetched =
                rest.exchange(
                        url("/api/v1/members/" + id),
                        HttpMethod.GET,
                        new HttpEntity<Void>(headers),
                        String.class);
        assertEquals(HttpStatus.OK, fetched.getStatusCode());
    }

    @Test
    void viewerCannotCreateMember() throws Exception {
        String token = login("viewer", "Viewer@12345");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response =
                rest.postForEntity(
                        url("/api/v1/members"),
                        new HttpEntity<String>(memberJson("VIEW-MRN"), headers),
                        String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    private String login(String username, String password) throws Exception {
        ResponseEntity<String> response =
                rest.postForEntity(
                        url("/api/v1/auth/login"),
                        new HttpEntity<String>(
                                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}",
                                jsonHeaders()),
                        String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode node = mapper.readTree(response.getBody());
        return node.get("accessToken").asText();
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private String memberJson(String mrn) {
        return "{\"mrn\":\""
                + mrn
        + "\",\"firstName\":\"Test\",\"lastName\":\"Member\",\"dateOfBirth\":\"1980-01-01\","
        + "\"gender\":\"OTHER\",\"planId\":\"P1\",\"enrollmentDate\":\"2020-01-01\"}";
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
