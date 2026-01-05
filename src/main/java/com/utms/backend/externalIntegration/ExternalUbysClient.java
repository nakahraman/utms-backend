package com.utms.backend.externalIntegration;

import com.utms.backend.model.enums.Role;

public interface ExternalUbysClient {
    boolean authenticate(String username, String password);

    Role fetchRole(String username);
}
