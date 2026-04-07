package com.btl.snaker.service;

import com.btl.snaker.entity.Role;
import com.btl.snaker.entity.User;
import com.btl.snaker.mapper.UserMapper;
import com.btl.snaker.payload.ResponseData;
import com.btl.snaker.payload.request.SignupRequest;
import com.btl.snaker.repository.UserRepository;
import com.btl.snaker.service.imp.LoginServiceImp;
import com.btl.snaker.utils.JwtUtilHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class LoginService implements LoginServiceImp {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtilHelper jwtUtilHelper;

    @Override
    public ResponseData checkLogin(String email, String password) {
        ResponseData responseData = new ResponseData();
        User user = userRepository.findByEmail(email);
        if(user!=null && passwordEncoder.matches(password, user.getPassword())) {
            List<String> roles = new ArrayList<>();
            roles.add(user.getRole().getRoleName());
            String token = jwtUtilHelper.generateTokens(email, roles);
            user.setToken(token);
            userRepository.save(user);
            responseData.setData(UserMapper.toUserDTO(user));
            responseData.setSuccess(true);
        }
        else{
            responseData.setSuccess(false);
            responseData.setDescription("Wrong password");
        }
        return responseData;
    }

    @Override
    public Boolean isEmailExist(String email) {
        return userRepository.findByEmail(email) != null;
    }

    @Override
    @Transactional
    public ResponseData signup(SignupRequest signupRequest) {
        ResponseData responseData = new ResponseData();
        if(isEmailExist(signupRequest.getEmail())) {
            responseData.setSuccess(false);
            responseData.setDescription("Email already exists");
            return responseData;
        }
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setFullname(signupRequest.getFullname());
        user.setCreatedAt(new Date());
        Role role = new Role();
        role.setId(2L);
        user.setRole(role);
        user.setActive(1);
        try{
            List<String> roles = new ArrayList<>();
            roles.add(role.getRoleName());
            String token = jwtUtilHelper.generateTokens(signupRequest.getEmail(), roles);
            user.setToken(token);
            userRepository.save(user);
            responseData.setData(UserMapper.toUserDTO(user));
            responseData.setSuccess(true);
            responseData.setDescription("Success");
        } catch (Exception e) {
            responseData.setSuccess(false);
            responseData.setDescription("Fail");
        }
        return responseData;
    }
}
