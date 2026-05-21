package com.revy.example.user.command.impl;

import com.revy.example.domain.user.User;
import com.revy.example.domain.user.exception.EmailAlreadyExistsException;
import com.revy.example.domain.user.exception.UserNotFoundException;
import com.revy.example.user.command.UserCommand;
import com.revy.example.user.command.dto.RegisterUserCommand;
import com.revy.example.user.reader.UserReader;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class UserCommandImpl implements UserCommand {

    private final EntityManager entityManager;
    private final UserReader    userReader;

    @Override
    public Long register(RegisterUserCommand command) {
        if (userReader.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException();
        }

        User user = User.create(command.email(), command.encodedPassword(), command.name());
        entityManager.persist(user);
        // TODO:REVY - EVENT 발행(UserRegistered) - commit after
        return user.getId();
    }

    @Override
    public void updateName(Long userId, String name) {
        loadUser(userId).updateName(name);
        // TODO:REVY - EVENT 발행(UserNameUpdated) - commit after
    }

    @Override
    public void changePassword(Long userId, String encodedPassword) {
        loadUser(userId).updatePassword(encodedPassword);
        // TODO:REVY - EVENT 발행(UserPasswordChanged) - commit after
    }

    @Override
    public void delete(Long userId) {
        entityManager.remove(loadUser(userId));
        // TODO:REVY - EVENT 발행(UserDeleted) - commit after
    }

    private User loadUser(Long userId) {
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new UserNotFoundException();
        }
        return user;
    }
}
