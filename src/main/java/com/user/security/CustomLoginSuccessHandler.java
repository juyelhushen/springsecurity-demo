package com.user.security;

import com.user.Implimentation.CustomUserDetails;
import com.user.model.User;
import com.user.model.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userDetails.getUser();
        if (user.getFailedAttempt() > 0){
            userService.resetFailedAttempts(user.getEmail());
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

    //    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                        Authentication authentication) throws IOException, ServletException {
//
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//        User user = userDetails.getUser();
//        if (user.getFailedAttempt() > 0) {
//            System.out.println("invoking");
//            userService.resetFailedAttempts(user.getEmail());
//        }
//        super.onAuthenticationSuccess(request, response, authentication);
//    }
}
