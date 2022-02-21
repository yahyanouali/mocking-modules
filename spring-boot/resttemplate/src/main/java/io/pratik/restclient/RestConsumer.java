
package io.pratik.restclient;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.pratik.apis.models.Product;

public class RestConsumer {

    private final Logger log = LoggerFactory.getLogger(RestConsumer.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String resourceUrl = "http://localhost:8080/products";

    public String getProductAsJson() throws JsonProcessingException {
        ResponseEntity<String> response = restTemplate.getForEntity(resourceUrl, String.class);
        String productsJson = response.getBody();

        log.info("status code is: '{}'", response.getStatusCode());
        log.info("result as json: {}", productsJson);

        return productsJson;
    }

    public List<Product> getProducts() {
        ResponseEntity<List> response = restTemplate.getForEntity(resourceUrl, List.class);
        List<Product> products = response.getBody();

        log.info("status code is: '{}'", response.getStatusCode());
        log.info("result as object : {}", products);

        return products;
    }

    public List<Product> getProductObjects() {
        //RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        List<?> products = restTemplate.getForObject(resourceUrl, List.class);

        log.info("no status code because of getForObject");
        log.info("result as object : {}", products);

        return (List<Product>) products;
    }

    public HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = restTemplate.headForHeaders(resourceUrl);

        log.info("headers: {}", httpHeaders);

        return httpHeaders;
    }

    public String createProduct() {
        Product product = new Product("Television", "Samsung", 1145.67, "S001");
        HttpEntity<Product> request = new HttpEntity<>(product);
        String productCreateResponse = restTemplate.postForObject(resourceUrl, request, String.class);

        log.info("product created with postForObject : {}", productCreateResponse);

        return productCreateResponse;
    }

    public ResponseEntity<String> createProductWithExchange() {
        HttpEntity<Product> request = new HttpEntity<>(new Product("Television", "Samsung", 1145.67, "S001"));
        ResponseEntity<String> productCreateResponse = restTemplate.exchange(resourceUrl, HttpMethod.POST, request, String.class);

        log.info("product created with exchange : {}", productCreateResponse);

        return productCreateResponse;
    }

    public void updateProductWithExchange() {
        HttpEntity<Product> request = new HttpEntity<>(new Product("Television", "YAHYA_MARK", 1145.67, "S001"));
        restTemplate.exchange(resourceUrl, HttpMethod.PUT, request, Void.class);
    }


    public void getProductAsStream() {
        final Product fetchProductRequest = new Product("Television", "Samsung", 1145.67, "S001");

        // a callback to construct the request by writing product object to body OutputStream
        RequestCallback requestCallback = request -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(request.getBody(), fetchProductRequest);

            request.getHeaders().setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));
        };

        // a callback to copy the response InputStream into a file
        ResponseExtractor<Void> responseExtractor = response -> {
            Path path = Paths.get("output.json");
            Files.copy(response.getBody(), path);
            return null;
        };

        restTemplate.execute(resourceUrl, HttpMethod.GET, requestCallback, responseExtractor);
    }


    public void submitProductForm() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("id", "1");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(resourceUrl + "/form", request, String.class);

        log.info(response.getBody());
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        int connectTimeout = 5000;
        int readTimeout = 5000;
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(connectTimeout);
        clientHttpRequestFactory.setReadTimeout(readTimeout);
        return clientHttpRequestFactory;
    }

    public void createProductWithLocation() {
        HttpEntity<Product> request = new HttpEntity<>(new Product("Television", "Samsung", 1145.67, "S001"));
        URI location = restTemplate.postForLocation(resourceUrl, request);

        log.info("the location is {}", location);
    }

    public void getAllowedOps() {
        Set<HttpMethod> optionsForAllow = restTemplate.optionsForAllow(resourceUrl);

        log.info("allowed options: {}", optionsForAllow);
    }

    public void getProductWithError() {
        RestTemplate customRestTemplate;
        RestTemplateCustomizer customizers = template -> template.setErrorHandler(new CustomErrorHandler());
        customRestTemplate = new RestTemplateBuilder(customizers).build();
        log.info("Default error handler:: {}", customRestTemplate.getErrorHandler());

        String errorUrl = "http://localhost:8080/product/error";

        try {
            Product product = customRestTemplate.getForObject(errorUrl, Product.class);
            log.info("result: {}", product);
        } catch (RestServiceException ex) {
            log.info("error occurred:: [{}] in service:: [{}]", ex.getError(), ex.getServiceName());
        }

    }

    public void getProductWithMessageTransformer() {
        RestTemplate restTemplate = new RestTemplate();

        RestTemplateCustomizer customizers = new RestTemplateCustomizer() {

            @Override
            public void customize(RestTemplate restTemplate) {
                restTemplate.setErrorHandler(new CustomErrorHandler());

            }
        };
        RestTemplateBuilder builder = new RestTemplateBuilder(customizers);
        // restTemplate = new RestTemplate();// builder.build();
        restTemplate = builder.build();

        System.out.println("Default error handler::" + restTemplate.getErrorHandler());
        String resourceUrl
                = "http://localhost:8080/product/error";
        try {
            Product product
                    = restTemplate.getForObject(resourceUrl, Product.class);

        } catch (RestServiceException ex) {
            System.out.println("error occured: [" + ex.getError() + "] in service:: " + ex.getServiceName());
        }

    }

    public void getProductAsXML() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(getXmlMessageConverter());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String productID = "P123445";

        String resourceUrl
                = "http://localhost:8080/products/" + productID;
        ResponseEntity<Product> response =
                restTemplate.exchange(resourceUrl, HttpMethod.GET, entity, Product.class, "1");
        Product resource = response.getBody();
    }

    private List<HttpMessageConverter<?>> getXmlMessageConverter() {
        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAnnotatedClasses(Product.class);
        MarshallingHttpMessageConverter marshallingConverter =
                new MarshallingHttpMessageConverter(marshaller);

        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(marshallingConverter);
        return converters;
    }


    public static void main(String[] args) throws JsonProcessingException {
        RestConsumer restConsumer = new RestConsumer();
		
		/*restConsumer.getProductAsJson();
		restConsumer.getProducts();
		restConsumer.getProductObjects();
		
		restConsumer.getHeaders(); restConsumer.createProduct();
		restConsumer.getAllowedOps(); restConsumer.updateProductWithExchange();
		restConsumer.getProductAsStream();
		restConsumer.createProductWithLocation();
		*/
        //restConsumer.getProductWithError();
        restConsumer.getProductWithError();

    }

}
