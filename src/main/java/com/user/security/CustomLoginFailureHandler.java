package com.user.security;

import com.user.model.User;
import com.user.model.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserService userService;


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("email");
        User user = userService.getByEmail(email);

        if (user != null) {
            if (user.isEnabled() && user.isAccountNonLocked()) {
                if (user.getFailedAttempt() < userService.MAX_FAILED_ATTEMPTS - 1){
                    userService.increaseFailedAttempts(user);
                } else {
                    userService.lock(user);
                    exception = new LockedException("Your account has been locked due to 3 failed attempts" +
                            ". It will be unlocked after 24 hours");
                }
            } else if (!user.isAccountNonLocked()) {
                if (userService.unlock(user)) {
                    exception = new LockedException("Your account has been unlocked. Please try to login again.");
                }
                
            }
        }

        super.setDefaultFailureUrl("/login/error");
        super.onAuthenticationFailure(request, response, exception);
    }
}
