package com.btl.snaker.controller;

import com.btl.snaker.entity.Product;
import com.btl.snaker.payload.ResponseData;
import com.btl.snaker.repository.ProductRepository;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    @Autowired
    private ProductRepository productRepository;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    private static final String[] MODELS = {
        "gemini-2.0-flash",
        "gemini-2.0-flash-lite",
        "gemini-2.5-flash",
        "gemini-2.5-flash-lite",
        "gemini-2.0-flash-001",
        "gemini-2.0-flash-lite-001"
    };

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestParam String message) {
        ResponseData responseData = new ResponseData();
        if (message == null || message.trim().isEmpty()) {
            responseData.setSuccess(false);
            responseData.setDescription("Message cannot be empty");
            return new ResponseEntity<>(responseData, HttpStatus.BAD_REQUEST);
        }
        try {
            // Gọi Gemini lấy text
            String botText = callGeminiWithFallback(message);

            // Tìm sản phẩm liên quan để hiển thị ảnh
            List<Map<String, Object>> suggestedProducts = findRelatedProducts(message, botText);

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("text", botText);
            result.put("products", suggestedProducts);

            responseData.setSuccess(true);
            responseData.setData(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("text", "Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng liên hệ hotline 0367468257.");
            result.put("products", new java.util.ArrayList<>());
            responseData.setSuccess(true);
            responseData.setData(result);
        }
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    private List<Map<String, Object>> findRelatedProducts(String message, String botText) {
        String combined = (message + " " + botText).toLowerCase();
        List<Product> allProducts = productRepository.findAll();

        return allProducts.stream()
                .filter(p -> {
                    String name = p.getName() == null ? "" : p.getName().toLowerCase();
                    String brand = p.getBrand() != null ? p.getBrand().getName().toLowerCase() : "";
                    return combined.contains(name) || combined.contains(brand) ||
                           (p.getName() != null && combined.contains(p.getName().split(" ")[0].toLowerCase()));
                })
                .limit(4)
                .map(p -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", p.getId());
                    m.put("name", p.getName());
                    m.put("image", p.getImage());
                    m.put("price", p.getPrice());
                    m.put("brand", p.getBrand() != null ? p.getBrand().getName() : "");
                    return m;
                })
                .collect(Collectors.toList());
    }

    private String callGeminiWithFallback(String userMessage) throws Exception {
        List<Product> products = productRepository.findAll();
        String productContext = products.stream()
                .map(p -> String.format("- %s | Thương hiệu: %s | Loại: %s | Giới tính: %s | Giá: %,dđ",
                        p.getName(),
                        p.getBrand() != null ? p.getBrand().getName() : "N/A",
                        p.getCategory() != null ? p.getCategory().getName() : "N/A",
                        p.getGender() != null ? p.getGender() : "Unisex",
                        p.getPrice()))
                .collect(Collectors.joining("\n"));

        String systemPrompt = "Bạn là trợ lý tư vấn giày thông minh của Sneaker Shop. " +
                "Hãy trả lời bằng tiếng Việt, thân thiện, ngắn gọn và hữu ích.\n\n" +
                "THÔNG TIN SHOP:\n" +
                "- Hotline: 0367468257\n" +
                "- Email: thangdc29@gmail.com\n" +
                "- Địa chỉ: Km10, Nguyễn Trãi, Hà Đông, Hà Nội\n" +
                "- Giao hàng toàn quốc, phí ship 30,000đ, miễn phí khi tự đến lấy\n" +
                "- Đổi trả trong 7 ngày\n" +
                "- Thanh toán: COD hoặc chuyển khoản MB Bank 1001234569666 (Đinh Công Thắng)\n" +
                "- Voucher: đơn 1tr giảm 10%, 3tr giảm 12%, 7tr giảm 14%, 10tr giảm 15%\n\n" +
                "DANH SÁCH SẢN PHẨM HIỆN CÓ:\n" + productContext + "\n\n" +
                "Hãy tư vấn dựa trên danh sách sản phẩm thực tế trên. " +
                "Nếu không có sản phẩm phù hợp, hãy thành thật nói và gợi ý khách liên hệ hotline. " +
                "Trả lời ngắn gọn, tối đa 200 từ.";

        JSONObject requestBody = buildRequestBody(systemPrompt, userMessage);

        for (String model : MODELS) {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + geminiApiKey;
            try {
                String result = callUrl(url, requestBody);
                if (result != null) {
                    System.out.println("✅ Using model: " + model);
                    return result;
                }
            } catch (Exception e) {
                System.out.println("❌ Model " + model + " failed: " + e.getMessage());
            }
        }
        return "Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng liên hệ hotline 0367468257.";
    }

    private JSONObject buildRequestBody(String systemPrompt, String userMessage) {
        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();

        JSONObject systemContent = new JSONObject();
        systemContent.put("role", "user");
        JSONArray systemParts = new JSONArray();
        systemParts.put(new JSONObject().put("text", systemPrompt));
        systemContent.put("parts", systemParts);
        contents.put(systemContent);

        JSONObject modelAck = new JSONObject();
        modelAck.put("role", "model");
        JSONArray modelParts = new JSONArray();
        modelParts.put(new JSONObject().put("text", "Tôi đã hiểu. Tôi sẽ tư vấn dựa trên thông tin shop và sản phẩm thực tế."));
        modelAck.put("parts", modelParts);
        contents.put(modelAck);

        JSONObject userContent = new JSONObject();
        userContent.put("role", "user");
        JSONArray userParts = new JSONArray();
        userParts.put(new JSONObject().put("text", userMessage));
        userContent.put("parts", userParts);
        contents.put(userContent);

        requestBody.put("contents", contents);
        return requestBody;
    }

    private String callUrl(String url, JSONObject requestBody) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(requestBody.toString(), "UTF-8"));

            try (CloseableHttpResponse response = client.execute(post)) {
                String responseStr = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject json = new JSONObject(responseStr);
                if (!json.has("candidates")) {
                    System.out.println("No candidates from " + url.split("models/")[1].split(":")[0] + ": " +
                            responseStr.substring(0, Math.min(150, responseStr.length())));
                    return null;
                }
                return json.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            }
        }
    }
}
