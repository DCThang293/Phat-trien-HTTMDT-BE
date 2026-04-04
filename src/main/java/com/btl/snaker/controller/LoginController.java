package com.btl.snaker.controller;

import com.btl.snaker.entity.Role;
import com.btl.snaker.entity.User;
import com.btl.snaker.payload.ResponseData;
import com.btl.snaker.payload.request.SignupRequest;
import com.btl.snaker.repository.UserRepository;
import com.btl.snaker.service.imp.LoginServiceImp;
import com.btl.snaker.utils.JwtUtilHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private LoginServiceImp loginServiceImp;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtilHelper jwtUtilHelper;

    @RequestMapping(value = "/create-admin", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<?> createAdmin() {
        ResponseData responseData = new ResponseData();
        try {
            // Xóa admin cũ nếu có
            User existingAdmin = userRepository.findByEmail("admin@sneaker.com");
            if (existingAdmin != null) {
                userRepository.delete(existingAdmin);
            }

            // Tạo admin mới
            User admin = new User();
            admin.setEmail("admin@sneaker.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setFullname("Administrator");
            admin.setPhone("0900000000");
            admin.setActive(1);
            admin.setCreatedAt(new Date());
            
            Role role = new Role();
            role.setId(1L);
            admin.setRole(role);
            
            userRepository.save(admin);
            
            responseData.setSuccess(true);
            responseData.setDescription("Admin created successfully. Email: admin@sneaker.com, Password: Admin@123");
        } catch (Exception e) {
            responseData.setSuccess(false);
            responseData.setDescription("Error: " + e.getMessage());
        }
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestParam String email, @RequestParam String password) {
        ResponseData responseData = new ResponseData();
        responseData = loginServiceImp.checkLogin(email, password);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        ResponseData responseData = loginServiceImp.signup(signupRequest);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestParam String email,
                                              @RequestParam String fullName) {
        ResponseData responseData = new ResponseData();
        try {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                // Tạo tài khoản mới nếu chưa có
                user = new User();
                user.setEmail(email);
                user.setFullname(fullName);
                user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                user.setActive(1);
                user.setCreatedAt(new java.util.Date());
                Role role = new Role();
                role.setId(2L);
                user.setRole(role);
            }
            List<String> roles = new ArrayList<>();
            roles.add("USER");
            String token = jwtUtilHelper.generateTokens(email, roles);
            user.setToken(token);
            userRepository.save(user);
            responseData.setSuccess(true);
            responseData.setData(com.btl.snaker.mapper.UserMapper.toUserDTO(user));
        } catch (Exception e) {
            responseData.setSuccess(false);
            responseData.setDescription("Lỗi: " + e.getMessage());
        }
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
}