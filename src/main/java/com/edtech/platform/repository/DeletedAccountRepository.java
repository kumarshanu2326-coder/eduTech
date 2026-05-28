package com.edtech.platform.repository;

import com.edtech.platform.entity.DeletedAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeletedAccountRepository extends JpaRepository<DeletedAccount, Long> {
    List<DeletedAccount> findAllByOrderByActionAtDesc();
    List<DeletedAccount> findByRoleOrderByActionAtDesc(String role);
}