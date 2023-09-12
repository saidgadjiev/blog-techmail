package ru.gadjini.blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.gadjini.blog.common.Messages;
import ru.gadjini.blog.controller.UserApiDelegate;
import ru.gadjini.blog.dao.UserRepository;
import ru.gadjini.blog.model.MessageResponse;
import ru.gadjini.blog.model.User;
import ru.gadjini.blog.model.UserUpdate;

import java.util.List;

@Service
public class UserService implements UserApiDelegate {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ResponseEntity<User> userCreate(String nickname, User profile) {
        profile.setNickname(nickname);
        boolean created = userRepository.create(profile);

        if (created) {
            return ResponseEntity.status(HttpStatus.CREATED).body(profile);
        } else {
            List<User> byNickNameOrEmail = userRepository.getBynicknameOrEmail(nickname, profile.getEmail());

            return (ResponseEntity<User>) (Object) ResponseEntity.status(HttpStatus.CONFLICT).body(byNickNameOrEmail);
        }
    }

    @Override
    public ResponseEntity<User> userGetOne(String nickname) {
        User byNickName = userRepository.getBynickname(nickname);

        if (byNickName == null) {
            return (ResponseEntity<User>) (Object) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(Messages.USER_NOT_FOUND + nickname));
        }

        return ResponseEntity.ok(byNickName);
    }

    @Override
    public ResponseEntity<User> userUpdate(String nickname, UserUpdate profile) {
        User updated = userRepository.update(nickname, profile);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            if (!profile.isEmailSet()) {
                MessageResponse messageResponse = new MessageResponse(
                        Messages.USER_NOT_FOUND + nickname
                );
                return (ResponseEntity<User>) (Object) ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
            }
            String alreadyUsedEmailNickname = userRepository.getNicknameByEmail(profile.getEmail());
            if (!StringUtils.hasLength(alreadyUsedEmailNickname)) {
                MessageResponse messageResponse = new MessageResponse(
                        Messages.USER_NOT_FOUND + nickname
                );
                return (ResponseEntity<User>) (Object) ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
            }
            MessageResponse messageResponse = new MessageResponse("This email is already registered by user: " + alreadyUsedEmailNickname);
            return (ResponseEntity<User>) (Object) ResponseEntity.status(HttpStatus.CONFLICT).body(messageResponse);
        }
    }
}
