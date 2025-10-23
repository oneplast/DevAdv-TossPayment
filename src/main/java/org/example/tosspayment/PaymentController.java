package org.example.tosspayment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final RestTemplate restTemplate;
    private final ObjectMapper om = new ObjectMapper();

    @GetMapping("/success")
    public String success() {
        return "success";
    }

    public record TossPaymentRequest(
            String paymentKey,
            String orderId,
            Long amount
    ) {
    }

    @ResponseBody
    @PostMapping("/confirm")
    public String confirm(@RequestBody TossPaymentRequest request) {
        // 시크릿키 작업
        String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
        String target = widgetSecretKey + ":";
        String encryptedSecretKey = "Basic " + Base64.getEncoder()
                .encodeToString(target.getBytes(StandardCharsets.UTF_8));

        // 헤더 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", encryptedSecretKey);

        Map<String, Object> requestMap = om.convertValue(request, new TypeReference<>() {
        });

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestMap, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.tosspayments.com/v1/payments/confirm",
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("성공!");
            return "success";
        } else {
            log.info("실패!");
            return "fail";
        }
    }
}
