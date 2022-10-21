package com.user.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service
@Transactional
public class UserService {

    public static final int MAX_FAILED_ATTEMPTS = 3;  //limit attempt

    public static final long LOCK_TIME_DURATION = 10 * 60 * 1000 ;

    @Autowired
    private UserRepository userRepository;

    public User save(User user) {
        boolean updateExistingUser = user.getId() != null;
        if (updateExistingUser) {
            User existingUser = userRepository.findById(user.getId()).get();
            if (user.getPassword().isEmpty()) {
                user.setPassword(existingUser.getPassword());
            } else {
                encodedPassword(user);
            }
        }else {
            encodedPassword(user);
        }
        return userRepository.save(user);
    }



    public User updateAccount(User user) {
        User existingUser = userRepository.findById(user.getId()).get();
        if (!user.getPassword().isEmpty()){
            existingUser.setPassword(user.getPassword());
            encodedPassword(existingUser);
        }
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        return userRepository.save(existingUser);
    }


    public void encodedPassword(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
    }


    public User get(Long id) {
        return userRepository.findById(id).get();
    }


    public void updateUserEnableStatus(Long id , boolean enabled){
        userRepository.updateEnableStatus(id,enabled);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email);
    }



    // emailValidater
    public boolean isUniqueEmailViolated(Long id ,String email) {
        boolean isUniqueEmailViolated = false;

        User user = userRepository.findByEmail(email);
        if (user == null) return false;
        boolean isCreatingNew = (id == null || id == 0);

        if (isCreatingNew) {
            if (user != null) isUniqueEmailViolated = true;
        } else {
            if (user.getId() != id) {
                isUniqueEmailViolated = true;
            }
        }
        return isUniqueEmailViolated;
    }


    //FAILEDATTEMPTS

    public void increaseFailedAttempts(User user){
        int newFailedAttempts = user.getFailedAttempt() + 1;
        userRepository.updateFailedAttempt(newFailedAttempts,user.getEmail());
    }

    public void lock(User user) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());
        userRepository.save(user);
    }


    //unlock process
    public boolean unlock(User user) {
        long lockTimeInMillis = user.getLockTime().getTime();
        long currentTimeInMillis = System.currentTimeMillis();

        if (lockTimeInMillis + LOCK_TIME_DURATION < currentTimeInMillis){
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            user.setFailedAttempt(0);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public void resetFailedAttempts(String email) {
        userRepository.updateFailedAttempt(0,email);
    }
}
