package ru.gadjini.blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.gadjini.blog.controller.ServiceApiDelegate;
import ru.gadjini.blog.dao.ForumRepository;
import ru.gadjini.blog.dao.PostRepository;
import ru.gadjini.blog.dao.ThreadRepository;
import ru.gadjini.blog.dao.UserRepository;

@Service
public class ServiceApiDelegateImpl implements ServiceApiDelegate {

    private final UserRepository userRepository;

    private final PostRepository postRepository;

    private final ThreadRepository threadRepository;

    private final ForumRepository forumRepository;

    @Autowired
    public ServiceApiDelegateImpl(UserRepository userRepository,
                                  PostRepository postRepository,
                                  ThreadRepository threadRepository,
                                  ForumRepository forumRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.threadRepository = threadRepository;
        this.forumRepository = forumRepository;
    }

    @Override
    public ResponseEntity<Void> clear() {
        postRepository.truncate();
        threadRepository.truncate();
        forumRepository.truncate();
        userRepository.truncate();

        return ResponseEntity.ok().build();
    }
}
