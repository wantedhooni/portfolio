package com.revy.example.command;

import com.revy.example.domain.User;
import com.revy.example.domain.UserDetail;
import com.revy.example.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InitRunner implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) throws Exception {
        for(int i=1;i<10;i++){
            userRepository.save(User.createNewUser(String.format("abc%s@gmail.com", i), passwordEncoder.encode("1234"), new UserDetail()));
        }
    }
}


