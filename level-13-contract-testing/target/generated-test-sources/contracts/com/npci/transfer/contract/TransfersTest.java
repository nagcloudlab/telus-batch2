package com.npci.transfer.contract;

import com.npci.transfer.contract.BaseContractTest;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import io.restassured.response.ResponseOptions;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;

@SuppressWarnings("rawtypes")
public class TransfersTest extends BaseContractTest {

	@Test
	public void validate_shouldReturnBadRequestForInsufficientBalance() throws Exception {
		// given:
			MockMvcRequestSpecification request = given()
					.header("Content-Type", "application/json")
					.body("{\"sourceUPI\":\"charlie@okaxis\",\"destinationUPI\":\"bob@paytm\",\"amount\":500.00}");

		// when:
			ResponseOptions response = given().spec(request)
					.post("/v1/transfers");

		// then:
			assertThat(response.statusCode()).isEqualTo(400);
			assertThat(response.header("Content-Type")).matches("application/json.*");

		// and:
			DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
			assertThatJson(parsedJson).field("['timestamp']").matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.*");
			assertThatJson(parsedJson).field("['status']").isEqualTo(400);
			assertThatJson(parsedJson).field("['error']").isEqualTo("Insufficient Balance");
			assertThatJson(parsedJson).field("['message']").matches(".*[Ii]nsufficient.*");
	}

	@Test
	public void validate_shouldReturnBadRequestForMissingFields() throws Exception {
		// given:
			MockMvcRequestSpecification request = given()
					.header("Content-Type", "application/json")
					.body("{\"sourceUPI\":\"alice@okaxis\",\"amount\":100.00}");

		// when:
			ResponseOptions response = given().spec(request)
					.post("/v1/transfers");

		// then:
			assertThat(response.statusCode()).isEqualTo(400);
			assertThat(response.header("Content-Type")).matches("application/json.*");

		// and:
			DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
			assertThatJson(parsedJson).field("['timestamp']").matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.*");
			assertThatJson(parsedJson).field("['status']").isEqualTo(400);
			assertThatJson(parsedJson).field("['error']").isEqualTo("Validation Error");
			assertThatJson(parsedJson).field("['message']").isEqualTo("Invalid request parameters");
			assertThatJson(parsedJson).field("['errors']").matches(".*[Dd]estination.*");
	}

	@Test
	public void validate_shouldReturnBadRequestForNegativeAmount() throws Exception {
		// given:
			MockMvcRequestSpecification request = given()
					.header("Content-Type", "application/json")
					.body("{\"sourceUPI\":\"alice@okaxis\",\"destinationUPI\":\"bob@paytm\",\"amount\":-100.00}");

		// when:
			ResponseOptions response = given().spec(request)
					.post("/v1/transfers");

		// then:
			assertThat(response.statusCode()).isEqualTo(400);
			assertThat(response.header("Content-Type")).matches("application/json.*");

		// and:
			DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
			assertThatJson(parsedJson).field("['timestamp']").matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.*");
			assertThatJson(parsedJson).field("['status']").isEqualTo(400);
			assertThatJson(parsedJson).field("['error']").isEqualTo("Validation Error");
			assertThatJson(parsedJson).field("['message']").isEqualTo("Invalid request parameters");
			assertThatJson(parsedJson).field("['errors']").matches(".*[Mm]inimum.*");
	}

	@Test
	public void validate_shouldReturnNotFoundForNonExistentAccount() throws Exception {
		// given:
			MockMvcRequestSpecification request = given()
					.header("Content-Type", "application/json")
					.body("{\"sourceUPI\":\"nonexistent@fake\",\"destinationUPI\":\"bob@paytm\",\"amount\":100.00}");

		// when:
			ResponseOptions response = given().spec(request)
					.post("/v1/transfers");

		// then:
			assertThat(response.statusCode()).isEqualTo(404);
			assertThat(response.header("Content-Type")).matches("application/json.*");

		// and:
			DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
			assertThatJson(parsedJson).field("['timestamp']").matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.*");
			assertThatJson(parsedJson).field("['status']").isEqualTo(404);
			assertThatJson(parsedJson).field("['error']").isEqualTo("Account Not Found");
			assertThatJson(parsedJson).field("['message']").matches(".*[Nn]ot [Ff]ound.*");
	}

	@Test
	public void validate_shouldSuccessfullyInitiateTransfer() throws Exception {
		// given:
			MockMvcRequestSpecification request = given()
					.header("Content-Type", "application/json")
					.body("{\"sourceUPI\":\"alice@okaxis\",\"destinationUPI\":\"bob@paytm\",\"amount\":500.00,\"remarks\":\"Payment for services\"}");

		// when:
			ResponseOptions response = given().spec(request)
					.post("/v1/transfers");

		// then:
			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.header("Content-Type")).matches("application/json.*");

		// and:
			DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
			assertThatJson(parsedJson).field("['transactionId']").matches("TXN-[0-9]{8,14}-[0-9]{4}");
			assertThatJson(parsedJson).field("['sourceUPI']").isEqualTo("alice@okaxis");
			assertThatJson(parsedJson).field("['destinationUPI']").isEqualTo("bob@paytm");
			assertThatJson(parsedJson).field("['amount']").isEqualTo(500.00);
			assertThatJson(parsedJson).field("['fee']").matches("-?(\\d*\\.\\d+|\\d+)");
			assertThatJson(parsedJson).field("['totalDebited']").matches("-?(\\d*\\.\\d+|\\d+)");
			assertThatJson(parsedJson).field("['status']").isEqualTo("SUCCESS");
			assertThatJson(parsedJson).field("['timestamp']").matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.*");
	}

}
