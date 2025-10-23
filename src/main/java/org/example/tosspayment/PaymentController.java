package org.example.tosspayment;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentController {

    @Value("${payment.targetUrl}")
    private String targetUrl;

    @Value("${payment.secretKey}")
    private String secretKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper om = new ObjectMapper();

    private static final String PRODUCT_NAME = "프리미엄 무선 이어폰";
    private static final String ORDER_ID = UUID.randomUUID().toString();
    private static final Long AMOUNT = 100L;

    @GetMapping("/")
    public String showIndex(Model model) {
        model.addAttribute("orderId", ORDER_ID);
        model.addAttribute("productName", PRODUCT_NAME);
        model.addAttribute("amount", AMOUNT);
        return "index";
    }

    @GetMapping("/fail")
    public String showFail(Model model) {
        model.addAttribute("orderId", ORDER_ID);
        model.addAttribute("productName", PRODUCT_NAME);
        model.addAttribute("amount", AMOUNT);

        return "fail";
    }

    @GetMapping("/success")
    public String showSuccess(Model model) {
        model.addAttribute("orderId", ORDER_ID);
        model.addAttribute("productName", PRODUCT_NAME);
        model.addAttribute("amount", AMOUNT);

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
        String target = secretKey + ":";
        String encryptedSecretKey = "Basic " + Base64.getEncoder().encodeToString(target.getBytes(UTF_8));

        // 헤더 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", encryptedSecretKey);

        Map<String, Object> requestMap = om.convertValue(request, new TypeReference<>() {
        });

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestMap, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                targetUrl,
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
