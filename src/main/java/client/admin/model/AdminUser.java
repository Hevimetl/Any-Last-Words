package client.admin.model;

import CommonTypes.UserInfo;
import AdminService.Admin;
import CommonTypes.AuthResult;
import java.util.ArrayList;
import java.util.List;

public class AdminUser {
    private Admin adminStub;
    private AuthResult user;
    private UserInfo[] users;

    public AdminUser(Admin adminStub, AuthResult user) {
        this.adminStub = adminStub;
        this.user = user;
    }

    public UserInfo[] getAllUsers() throws Exception {
        this.users = adminStub.getAllUsers(user.token);
        return users;
    }

    public UserInfo getUser(String username) throws Exception {
        return adminStub.getUser(user.token, username);
    }

    public UserInfo[] searchUsers(String query) {
        if (users == null) return new UserInfo[0];
        List<UserInfo> results = new ArrayList<>();
        for (UserInfo user : users) {
            if (user.username.toLowerCase().contains(query.toLowerCase())) {
                results.add(user);
            }
        }
        return results.toArray(new UserInfo[0]);
    }
}