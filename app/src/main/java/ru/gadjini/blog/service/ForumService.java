package ru.gadjini.blog.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.gadjini.blog.controller.ForumApiDelegate;
import ru.gadjini.blog.dao.ForumRepository;
import ru.gadjini.blog.model.Forum;

@Service
public class ForumService implements ForumApiDelegate {

    private final ForumRepository forumRepository;

    public ForumService(ForumRepository forumRepository) {
        this.forumRepository = forumRepository;
    }

    @Override
    public ResponseEntity<Forum> forumCreate(Forum forum) {
        boolean created = forumRepository.create(forum);

        if (created) {
            return ResponseEntity.status(HttpStatus.CREATED).body(forum);
        } else {
            Forum alreadyCreated = forumRepository.getBySlug(forum.getSlug());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(alreadyCreated);
        }
    }
}
