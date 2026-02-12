package org.example.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Strips the unsupported {@code extra_body} field from request bodies when calling
 * Groq's OpenAI-compatible API (api.groq.com), which rejects unknown parameters.
 */
@Configuration
public class GroqOpenAiRestClientConfig {

    @Bean
    public RestClientCustomizer groqExtraBodyStripperCustomizer(ObjectMapper objectMapper) {
        ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                                ClientHttpRequestExecution execution) throws IOException {
                byte[] out = body;
                if (body != null && body.length > 0 && isGroqRequest(request)) {
                    String json = new String(body, StandardCharsets.UTF_8);
                    if (json.contains("extra_body")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = objectMapper.readValue(json, Map.class);
                        if (map.remove("extra_body") != null) {
                            out = objectMapper.writeValueAsBytes(map);
                        }
                    }
                }
                // Use a request wrapper that sets Content-Length to the actual body size,
                // otherwise "unexpected end of stream" occurs when the body is modified.
                HttpRequest requestToUse = out != null && out.length != (body != null ? body.length : 0)
                    ? new ContentLengthRequest(request, out.length)
                    : request;
                return execution.execute(requestToUse, out);
            }
        };
        return builder -> builder.requestInterceptor(interceptor);
    }

    private static boolean isGroqRequest(HttpRequest request) {
        if (request.getURI() == null || request.getURI().getHost() == null) {
            return false;
        }
        return request.getURI().getHost().toLowerCase().contains("groq");
    }

    /** Wraps the request and overrides Content-Length so it matches the modified body. */
    private static final class ContentLengthRequest implements HttpRequest {
        private final HttpRequest delegate;
        private final HttpHeaders headers;

        ContentLengthRequest(HttpRequest delegate, int contentLength) {
            this.delegate = delegate;
            this.headers = new HttpHeaders();
            this.headers.addAll(delegate.getHeaders());
            this.headers.setContentLength(contentLength);
        }

        @Override
        public HttpMethod getMethod() {
            return delegate.getMethod();
        }

        @Override
        public java.net.URI getURI() {
            return delegate.getURI();
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}
