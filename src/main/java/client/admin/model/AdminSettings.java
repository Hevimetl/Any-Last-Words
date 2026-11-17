package client.admin.model;

import AdminService.Admin;
import CommonTypes.AuthResult;

public class AdminSettings {
    private Admin adminStub;
    private AuthResult user;

    public AdminSettings(Admin adminStub, AuthResult user) {
        this.adminStub = adminStub;
        this.user = user;
    }

    public Admin getAdminStub() {
        return adminStub;
    }

    public AuthResult getUser() {
        return user;
    }
}