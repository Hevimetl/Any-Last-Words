package client.common.model;


import CommonTypes.*;

public class Authentication {
    Login loginService;
    public Authentication(Login loginService){
        this.loginService = loginService;
    }

    public AuthResult authenticate(String userName, String password) throws InvalidCredentialsException, UserDoesNotExistException, UserAlreadyLoggedInException {
        return loginService.authenticate(userName, password);
    }
}
