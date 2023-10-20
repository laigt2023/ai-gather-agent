package com.gcloud.demo.uploaddemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.util.Enumeration;

import org.springframework.http.MediaType;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * @Author: laigt
 * @Date: 2023-10-20 9:02
 * @Desperation: TODO 接口转发
 */

@RestController
@RequestMapping("/face")
@Slf4j
public class ProxyController {

    @Value("${gcloud.beijing.face_http_proxy}")
    private String FACE_HTTP_PROXY = "http://127.0.0.1:13800";

    // 接口转发
    @RequestMapping("/**")
    public ResponseEntity<String> handleRequest(HttpServletRequest request) throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String method = request.getMethod();
        String path = getPath(request);
        // 去掉url中的/face
        path = path.substring(request.getContextPath().length()+"/face".length());
        URI targetUri = new URI(FACE_HTTP_PROXY + path);
        System.out.println(targetUri);
        HttpHeaders headers = getRequestHeaders(request);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate(getSecureHttpRequestFactory());
        if (method.equalsIgnoreCase(HttpMethod.GET.name())) {
            return restTemplate.exchange(targetUri, HttpMethod.GET, entity, String.class);
        } else if (method.equalsIgnoreCase(HttpMethod.POST.name())) {
            String requestBody = getRequestBody(request);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> postEntity = new HttpEntity<>(requestBody, headers);
            return restTemplate.exchange(targetUri, HttpMethod.POST, postEntity, String.class);
        } else {
            return ResponseEntity.badRequest().body("Unsupported request method: " + method);
        }
    }

    private String getPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo() != null ? request.getPathInfo() : "";
        return contextPath + servletPath + pathInfo;
    }

    private HttpHeaders getRequestHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            List<String> headerValues = Collections.list(request.getHeaders(headerName));
            headers.put(headerName, headerValues);
        }
        return headers;
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        return request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
    }

    private HttpComponentsClientHttpRequestFactory getSecureHttpRequestFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
            }
        } };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, null);

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(HttpClients.custom().setSSLSocketFactory(csf).build());

        return requestFactory;
    }
}
