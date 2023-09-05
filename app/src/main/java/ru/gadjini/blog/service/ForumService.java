package ru.gadjini.blog.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.gadjini.blog.controller.ForumApiDelegate;
import ru.gadjini.blog.dao.ForumRepository;
import ru.gadjini.blog.model.Forum;
import ru.gadjini.blog.model.MessageResponse;

@Service
public class ForumService implements ForumApiDelegate {

    private final ForumRepository forumRepository;

    public ForumService(ForumRepository forumRepository) {
        this.forumRepository = forumRepository;
    }

    @Override
    public ResponseEntity<Forum> forumCreate(Forum forum) {
        Forum created = forumRepository.create(forum);

        if (created != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } else {
            Forum alreadyCreated = forumRepository.getBySlug(forum.getSlug());
            if (alreadyCreated != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(alreadyCreated);
            }

            MessageResponse messageResponse = new MessageResponse(
                    "Can't find user with nickname: " + forum.getUser()
            );
            return (ResponseEntity<Forum>) (Object) ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
        }
    }

    @Override
    public ResponseEntity<Forum> forumGetOne(String slug) {
        Forum bySlug = forumRepository.getBySlug(slug);

        if (bySlug == null) {
            MessageResponse messageResponse = new MessageResponse(
                    "Can't find forum with slug: " + slug
            );

            return (ResponseEntity<Forum>) (Object) ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
        }
        return ResponseEntity.ok(bySlug);
    }
}
