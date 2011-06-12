package service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import domain.CustomerList;
import filter.SignatureHelper;

@ContextConfiguration(locations = { "classpath:service/client-config.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class CustomerServiceFunctionalTest {

	@Autowired
	private RestTemplate template;

	private HttpMessageConverterExtractor<CustomerList> responseExtractor;
	
	@Before
	public void setUp() {
		responseExtractor = new HttpMessageConverterExtractor<CustomerList>(CustomerList.class, template.getMessageConverters());
	}

	@Test
	public void shouldReturnCustomers() throws Exception {
		final String urlApp = "/rest-security/services/customers?";
		final String fullUrl = "http://localhost:8080" + urlApp;

		CustomerList list = template.execute(fullUrl, HttpMethod.GET,
				new RequestCallback() {
					@Override
					public void doWithRequest(ClientHttpRequest request) throws IOException {
						HttpHeaders headers = request.getHeaders();
						headers.add("Accept", "*/*");
						headers.add(SignatureHelper.APIKEY_HEADER, SignatureHelper.API_KEY);
						headers.add(SignatureHelper.TIMESTAMP_HEADER, "" + System.currentTimeMillis());
						try {
							headers.add(SignatureHelper.SIGNATURE_HEADER, 
									SignatureHelper.createSignature(headers, urlApp, SignatureHelper.PRIVATE_KEY));
						} catch (Exception e) {
							fail();
						}
					}
				}, responseExtractor);

		assertTrue(list.getCustomer().size() > 0);
	}

	@Test
	public void shouldReturn401WithBadSignature() throws Exception {
		final String urlApp = "/rest-security/services/customers?";
		final String fullUrl = "http://localhost:8080" + urlApp;

		try {
			template.execute(fullUrl, HttpMethod.GET, new RequestCallback() {
				@Override
				public void doWithRequest(ClientHttpRequest request) throws IOException {
					HttpHeaders headers = request.getHeaders();
					headers.add("Accept", "*/*");
					headers.add(SignatureHelper.APIKEY_HEADER, SignatureHelper.API_KEY);
					headers.add(SignatureHelper.TIMESTAMP_HEADER, "" + System.currentTimeMillis());
					headers.add(SignatureHelper.SIGNATURE_HEADER, "invalid");
				}
			}, responseExtractor);
			fail("Should have thrown exception");
		} catch (HttpClientErrorException e) {
			assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
		}
	}

}
