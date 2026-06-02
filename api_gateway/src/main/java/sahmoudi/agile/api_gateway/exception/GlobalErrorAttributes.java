package sahmoudi.agile.api_gateway.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> defaultAttributes = super.getErrorAttributes(request, options);
        
        Map<String, Object> problemDetails = new LinkedHashMap<>();
        problemDetails.put("type", "about:blank");
        problemDetails.put("title", defaultAttributes.get("error"));
        problemDetails.put("status", defaultAttributes.get("status"));
        problemDetails.put("detail", defaultAttributes.getOrDefault("message", "An error occurred"));
        problemDetails.put("instance", defaultAttributes.get("path"));
        
        return problemDetails;
    }
}
