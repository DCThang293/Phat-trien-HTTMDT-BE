package com.btl.snaker.repository;

import com.btl.snaker.entity.ReturnRequest;
import com.btl.snaker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    List<ReturnRequest> findByUser(User user);
    boolean existsByOrderIdAndUserId(Long orderId, Long userId);
    ReturnRequest findById(long id);
}
