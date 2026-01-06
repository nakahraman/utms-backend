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

    @Override
    public Role fetchRole(String username) {

        // Kullanıcı adına göre rol simülasyonu
        if (username.startsWith("std")) return Role.STUDENT;
        if (username.startsWith("fac")) return Role.FACULTY;
        if (username.startsWith("ygk")) return Role.YGK;
        if (username.startsWith("oidb")) return Role.OIDB;
        if (username.startsWith("ydyo")) return Role.YDYO;

        // Varsayılan rol
        return Role.STUDENT;
    }

}

