package ru.gadjini.blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.gadjini.blog.controller.ServiceApiDelegate;
import ru.gadjini.blog.dao.UserRepository;

@Service
public class ServiceApiDelegateImpl implements ServiceApiDelegate {

    private final UserRepository userRepository;

    @Autowired
    public ServiceApiDelegateImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ResponseEntity<Void> clear() {
        userRepository.truncate();

        return ResponseEntity.ok().build();
    }
}
