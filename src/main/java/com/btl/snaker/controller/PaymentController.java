package com.btl.snaker.controller;

import com.btl.snaker.payload.ResponseData;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/payment")
public class PaymentController {

    // Lấy thông tin tài khoản ngân hàng để chuyển khoản
    @GetMapping("/bank-info")
    public ResponseEntity<?> getBankInfo(@RequestParam String orderId, @RequestParam long amount) {
        ResponseData responseData = new ResponseData();
        try {
            Map<String, Object> bankInfo = new HashMap<>();
            bankInfo.put("bankName", "Ngân hàng TMCP Quân đội (MB Bank)");
            bankInfo.put("accountNumber", "1001234569666");
            bankInfo.put("accountName", "DINH CONG THANG");
            bankInfo.put("amount", amount);
            bankInfo.put("content", "Thanh toan don hang " + orderId);
            bankInfo.put("qrCode", "https://img.vietqr.io/image/MB-1001234569666-compact.png?amount=" + amount + "&addInfo=Thanh%20toan%20don%20hang%20" + orderId);
            
            responseData.setSuccess(true);
            responseData.setData(bankInfo);
            responseData.setDescription("Vui lòng chuyển khoản theo thông tin trên");
        } catch (Exception e) {
            responseData.setSuccess(false);
            responseData.setDescription("Lỗi: " + e.getMessage());
        }
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
}
