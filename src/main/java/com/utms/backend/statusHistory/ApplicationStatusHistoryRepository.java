package com.utms.backend.statusHistory;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, Long> {
    List<ApplicationStatusHistory> findByApplication_AppIdOrderByChangedAtDesc(Long appId);

    List<ApplicationStatusHistory> findByApplication_AppId(Long appId);}
