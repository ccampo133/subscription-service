package me.ccampo.subscriptionservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Primarily tests the web (controller) layer, but these are full E2E integration tests.
 *
 * @author Chris Campo
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WebIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    public static class TestMessage {
        public UUID id;
        public String type;
        public String content;
    }

    public static class TestSubscriptionResource {
        public UUID id;
        public String name;
        public List<String> messageTypes;
        public List<TestMessage> messages;
        public Map<String, Long> messageCountsByType;
    }

    @Test
    public void testSubscriptionIsCreated() {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "foo");
        params.add("messageTypes", "type9,type2,type5");
        final ResponseEntity<TestSubscriptionResource> response =
                restTemplate.postForEntity("/subscriptions", params, TestSubscriptionResource.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().id).isNotNull();
        assertThat(response.getHeaders().get("Location")).hasSize(1)
                .first().isEqualTo("http://localhost:" + port + "/subscriptions/" + response.getBody().id);
        assertThat(response.getBody().name).isEqualTo("foo");
        assertThat(response.getBody().messageTypes).containsExactlyInAnyOrder("type9", "type2", "type5");
        assertThat(response.getBody().messages).isEmpty();
        assertThat(response.getBody().messageCountsByType)
                .containsOnly(entry("type9", 0L), entry("type2", 0L), entry("type5", 0L));
    }

    @Test
    public void testSubscriptionIsRetrieved() {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "foo");
        params.add("messageTypes", "type9,type2,type5");
        final ResponseEntity<TestSubscriptionResource> response =
                restTemplate.postForEntity("/subscriptions", params, TestSubscriptionResource.class);

        final ResponseEntity<TestSubscriptionResource> response2 =
                restTemplate.getForEntity("/subscriptions/" + response.getBody().id, TestSubscriptionResource.class);

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody().id).isNotNull();
        assertThat(response2.getBody().name).isEqualTo("foo");
        assertThat(response2.getBody().messageTypes).containsExactlyInAnyOrder("type9", "type2", "type5");
        assertThat(response2.getBody().messages).isEmpty();
        assertThat(response2.getBody().messageCountsByType)
                .containsOnly(entry("type9", 0L), entry("type2", 0L), entry("type5", 0L));
    }

    @Test
    public void testSubscriptionIsNotRetrieved404() {
        final ResponseEntity<TestSubscriptionResource> response =
                restTemplate.getForEntity("/subscriptions/" + UUID.randomUUID(), TestSubscriptionResource.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testSubscriptionNameAndTypesAreUpdated() {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "foo");
        params.add("messageTypes", "type9,type2,type5");
        final ResponseEntity<TestSubscriptionResource> response =
                restTemplate.postForEntity("/subscriptions", params, TestSubscriptionResource.class);

        final MultiValueMap<String, String> params2 = new LinkedMultiValueMap<>();
        params2.add("name", "bar");
        params2.add("messageTypes", "type1");

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final ResponseEntity<TestSubscriptionResource> response2 = restTemplate
                .exchange("/subscriptions/" + response.getBody().id, HttpMethod.PUT,
                        new HttpEntity<MultiValueMap>(params2, headers), TestSubscriptionResource.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody().id).isNotNull();
        assertThat(response2.getBody().name).isEqualTo("bar");
        assertThat(response2.getBody().messageTypes).containsExactly("type1");
        assertThat(response2.getBody().messages).isEmpty();
        assertThat(response2.getBody().messageCountsByType).containsExactly(entry("type1", 0L));
    }

    @Test
    public void testSubscriptionNameIsUpdated() {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "foo");
        params.add("messageTypes", "type9,type2,type5");
        final ResponseEntity<TestSubscriptionResource> response =
                restTemplate.postForEntity("/subscriptions", params, TestSubscriptionResource.class);

        final MultiValueMap<String, String> params2 = new LinkedMultiValueMap<>();
        params2.add("name", "bar");

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final ResponseEntity<TestSubscriptionResource> response2 = restTemplate
                .exchange("/subscriptions/" + response.getBody().id, HttpMethod.PUT,
                        new HttpEntity<MultiValueMap>(params2, headers), TestSubscriptionResource.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody().id).isNotNull();
        assertThat(response2.getBody().name).isEqualTo("bar");
        assertThat(response2.getBody().messageTypes).containsExactly("type9", "type2", "type5");
        assertThat(response2.getBody().messages).isEmpty();
        assertThat(response2.getBody().messageCountsByType)
                .containsOnly(entry("type9", 0L), entry("type2", 0L), entry("type5", 0L));
    }

    @Test
    public void testSubscriptionTypesAreUpdated() {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "foo");
        params.add("messageTypes", "type9,type2,type5");
        final ResponseEntity<TestSubscriptionResource> response =
                restTemplate.postForEntity("/subscriptions", params, TestSubscriptionResource.class);

        final MultiValueMap<String, String> params2 = new LinkedMultiValueMap<>();
        params2.add("messageTypes", "type1");

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final ResponseEntity<TestSubscriptionResource> response2 = restTemplate
                .exchange("/subscriptions/" + response.getBody().id, HttpMethod.PUT,
                        new HttpEntity<MultiValueMap>(params2, headers), TestSubscriptionResource.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody().id).isNotNull();
        assertThat(response2.getBody().name).isEqualTo("foo");
        assertThat(response2.getBody().messageTypes).containsExactly("type1");
        assertThat(response2.getBody().messages).isEmpty();
        assertThat(response2.getBody().messageCountsByType).containsExactly(entry("type1", 0L));
    }

    @Test
    public void testSubscriptionIsUpdatedWithNoContent() {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "foo");
        params.add("messageTypes", "type9,type2,type5");
        final ResponseEntity<TestSubscriptionResource> response =
                restTemplate.postForEntity("/subscriptions", params, TestSubscriptionResource.class);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final ResponseEntity<TestSubscriptionResource> response2 = restTemplate
                .exchange("/subscriptions/" + response.getBody().id, HttpMethod.PUT, new HttpEntity(headers),
                        TestSubscriptionResource.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response2.getBody()).isNull();
    }

    @Test
    public void testMessageIsCreated() {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("type", "type1");
        params.add("content", "hello world");
        final ResponseEntity<TestMessage> response = restTemplate.postForEntity("/messages", params, TestMessage.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().id).isNotNull();
        assertThat(response.getBody().type).isEqualTo("type1");
        assertThat(response.getBody().content).isEqualTo("hello world");
    }

    @Test
    public void testMessageIsCreatedAndReceivedByRelevantSubscriptions() {
        final MultiValueMap<String, String> p1 = new LinkedMultiValueMap<>();
        p1.add("name", "foo");
        p1.add("messageTypes", "type9,type5");
        final TestSubscriptionResource sub1 =
                restTemplate.postForObject("/subscriptions", p1, TestSubscriptionResource.class);

        final MultiValueMap<String, String> p2 = new LinkedMultiValueMap<>();
        p2.add("name", "bar");
        p2.add("messageTypes", "type1,type5");
        final TestSubscriptionResource sub2 =
                restTemplate.postForObject("/subscriptions", p2, TestSubscriptionResource.class);

        final MultiValueMap<String, String> p3 = new LinkedMultiValueMap<>();
        p3.add("type", "type1");
        p3.add("content", "hello world");
        final TestMessage msg1 = restTemplate.postForObject("/messages", p3, TestMessage.class);

        final MultiValueMap<String, String> p4 = new LinkedMultiValueMap<>();
        p4.add("type", "type5");
        p4.add("content", "5hello");
        final TestMessage msg2 = restTemplate.postForObject("/messages", p4, TestMessage.class);

        final MultiValueMap<String, String> p5 = new LinkedMultiValueMap<>();
        p5.add("type", "type9");
        p5.add("content", "9hello");
        final TestMessage msg3 = restTemplate.postForObject("/messages", p5, TestMessage.class);

        final TestSubscriptionResource sub3 =
                restTemplate.getForObject("/subscriptions/" + sub1.id, TestSubscriptionResource.class);

        final TestSubscriptionResource sub4 =
                restTemplate.getForObject("/subscriptions/" + sub2.id, TestSubscriptionResource.class);

        assertThat(sub3.messages).hasSize(2);
        assertThat(sub3.messages).usingFieldByFieldElementComparator().containsExactlyInAnyOrder(msg2, msg3);

        assertThat(sub4.messages).hasSize(2);
        assertThat(sub4.messages).usingFieldByFieldElementComparator().containsExactlyInAnyOrder(msg1, msg2);
    }
}
