package client.admin.model;

import AdminService.Admin;
import CommonTypes.AuthResult;
import CommonTypes.UserInfo;

import java.util.Arrays;
import java.util.List;

public class AdminUserDetails {
    private final Admin adminStub;
    private final AuthResult user;

    public AdminUserDetails(Admin adminStub, AuthResult user) {
        this.adminStub = adminStub;
        this.user = user;
    }

    public List<UserInfo> getAllUsers() throws Exception {
        return Arrays.asList(adminStub.getAllUsers(user.token));
    }

    public Admin getAdminStub() {
        return adminStub;
    }

    public AuthResult getUser() {
        return user;
    }
}