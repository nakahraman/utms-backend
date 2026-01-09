package com.utms.backend.externalIntegration;


import com.utms.backend.model.enums.Role;
import org.springframework.stereotype.Service;

@Service
public class MockUbysClient implements ExternalUbysClient {

    @Override
    public boolean authenticate(String username, String password) {

        return username.startsWith("std")
               || username.startsWith("fac")
               || username.startsWith("ygk")
               || username.startsWith("oidb")
               || username.startsWith("ydyo");
    }

}

