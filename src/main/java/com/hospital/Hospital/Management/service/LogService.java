package com.hospital.Hospital.Management.service;

import com.hospital.Hospital.Management.model.SystemLog;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogService {

    private final SystemLogRepository systemLogRepository;

    public void logActivity(User user, String action, String details) {
        SystemLog log = SystemLog.builder()
                .user(user)
                .action(action)
                .details(details)
                .build();
        systemLogRepository.save(log);
    }

    public void logSystemActivity(String action, String details) {
        SystemLog log = SystemLog.builder()
                .user(null)
                .action(action)
                .details(details)
                .build();
        systemLogRepository.save(log);
    }
}