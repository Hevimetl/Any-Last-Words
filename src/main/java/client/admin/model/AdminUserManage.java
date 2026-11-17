package client.admin.model;

import AdminService.Admin;
import CommonTypes.AuthResult;
import CommonTypes.UserInfo;

public class AdminUserManage {
    private final Admin adminStub;
    private final AuthResult user;
    private UserInfo currentUser;

    public AdminUserManage(Admin adminStub, AuthResult user, UserInfo currentUser) {
        this.adminStub = adminStub;
        this.user = user;
        this.currentUser = currentUser;
    }

    public void updateUser(String newUsername, String newPassword) throws Exception {
        adminStub.updateUser(user.token, currentUser.username, newUsername, newPassword);

        if (newUsername != null) {
            currentUser.username = newUsername;
        }
        if (newPassword != null) {
            currentUser.password = newPassword;
        }
    }

    public void deleteUser() throws Exception {
        adminStub.deleteUser(user.token, currentUser.username);
    }

    public UserInfo getCurrentUser() {
        return currentUser;
    }
}