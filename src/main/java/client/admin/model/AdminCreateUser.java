package client.admin.model;

import AdminService.Admin;
import CommonTypes.*;

public class AdminCreateUser {
    private final Admin adminStub;
    private final AuthResult user;

    public AdminCreateUser(Admin adminStub, AuthResult user) {
        this.adminStub = adminStub;
        this.user = user;
    }

    public void createUser(String username, String password, UserRole role)
            throws AdminPrivilegeRequiredException, UserAlreadyExistsException, AuthenticationException {
        adminStub.createUser(user.token, username, password, role);
    }
}