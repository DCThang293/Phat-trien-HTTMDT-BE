package com.btl.snaker.controller;

import com.btl.snaker.entity.Order;
import com.btl.snaker.entity.ReturnRequest;
import com.btl.snaker.entity.User;
import com.btl.snaker.payload.ResponseData;
import com.btl.snaker.repository.OrderRepository;
import com.btl.snaker.repository.ReturnRequestRepository;
import com.btl.snaker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/return-request")
public class ReturnRequestController {

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    // Khách hàng gửi yêu cầu trả hàng
    @PostMapping("/user/create")
    public ResponseEntity<?> createReturnRequest(
            @RequestParam long orderId,
            @RequestParam long userId,
            @RequestParam String reason) {
        ResponseData responseData = new ResponseData();
        try {
            if (returnRequestRepository.existsByOrderIdAndUserId(orderId, userId)) {
                responseData.setSuccess(false);
                responseData.setDescription("Bạn đã gửi yêu cầu trả hàng cho đơn này rồi");
                return new ResponseEntity<>(responseData, HttpStatus.OK);
            }
            Order order = orderRepository.findById(orderId);
            User user = userRepository.findById(userId);
            if (order == null || user == null) {
                responseData.setSuccess(false);
                responseData.setDescription("Không tìm thấy đơn hàng hoặc người dùng");
                return new ResponseEntity<>(responseData, HttpStatus.OK);
            }
            ReturnRequest request = new ReturnRequest();
            request.setOrder(order);
            request.setUser(user);
            request.setReason(reason);
            request.setStatus(0);
            request.setCreatedAt(new Date());
            returnRequestRepository.save(request);
            responseData.setSuccess(true);
            responseData.setDescription("Gửi yêu cầu trả hàng thành công");
        } catch (Exception e) {
            responseData.setSuccess(false);
            responseData.setDescription("Lỗi: " + e.getMessage());
        }
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    // Khách hàng xem yêu cầu của mình
    @GetMapping("/user/get")
    public ResponseEntity<?> getUserReturnRequests(@RequestParam long userId) {
        ResponseData responseData = new ResponseData();
        User user = userRepository.findById(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        if (user != null) {
            for (ReturnRequest r : returnRequestRepository.findByUser(user)) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", r.getId());
                map.put("orderId", r.getOrder().getId());
                map.put("reason", r.getReason());
                map.put("adminNote", r.getAdminNote());
                map.put("createdAt", r.getCreatedAt());
                map.put("status", r.getStatus() == 0 ? "Chờ xử lý" : r.getStatus() == 1 ? "Đã duyệt" : "Từ chối");
                result.add(map);
            }
        }
        responseData.setSuccess(true);
        responseData.setData(result);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    // Admin xem tất cả yêu cầu
    @GetMapping("/admin/get/all")
    public ResponseEntity<?> getAllReturnRequests() {
        ResponseData responseData = new ResponseData();
        List<Map<String, Object>> result = new ArrayList<>();
        for (ReturnRequest r : returnRequestRepository.findAll()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("orderId", r.getOrder().getId());
            map.put("userId", r.getUser().getId());
            map.put("userName", r.getUser().getFullname());
            map.put("userEmail", r.getUser().getEmail());
            map.put("userPhone", r.getUser().getPhone());
            map.put("reason", r.getReason());
            map.put("adminNote", r.getAdminNote());
            map.put("createdAt", r.getCreatedAt());
            map.put("status", r.getStatus() == 0 ? "Chờ xử lý" : r.getStatus() == 1 ? "Đã duyệt" : "Từ chối");
            result.add(map);
        }
        responseData.setSuccess(true);
        responseData.setData(result);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    // Admin duyệt hoặc từ chối
    @PostMapping("/admin/solve")
    public ResponseEntity<?> solveReturnRequest(
            @RequestParam long id,
            @RequestParam int status,
            @RequestParam(required = false, defaultValue = "") String adminNote) {
        ResponseData responseData = new ResponseData();
        ReturnRequest request = returnRequestRepository.findById(id);
        if (request == null) {
            responseData.setSuccess(false);
            responseData.setDescription("Không tìm thấy yêu cầu");
            return new ResponseEntity<>(responseData, HttpStatus.OK);
        }
        request.setStatus(status);
        request.setAdminNote(adminNote);
        returnRequestRepository.save(request);
        responseData.setSuccess(true);
        responseData.setDescription(status == 1 ? "Đã duyệt yêu cầu" : "Đã từ chối yêu cầu");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
}
