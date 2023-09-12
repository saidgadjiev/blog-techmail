package ru.gadjini.blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.gadjini.blog.common.Messages;
import ru.gadjini.blog.controller.ForumApiDelegate;
import ru.gadjini.blog.dao.ForumRepository;
import ru.gadjini.blog.dao.ThreadRepository;
import ru.gadjini.blog.dao.UserRepository;
import ru.gadjini.blog.model.Forum;
import ru.gadjini.blog.model.MessageResponse;
import ru.gadjini.blog.model.Thread;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ForumService implements ForumApiDelegate {

    private final ForumRepository forumRepository;

    private final ThreadRepository threadRepository;

    private final UserRepository userRepository;

    @Autowired
    public ForumService(ForumRepository forumRepository, ThreadRepository threadRepository,
                        UserRepository userRepository) {
        this.forumRepository = forumRepository;
        this.threadRepository = threadRepository;
        this.userRepository = userRepository;
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
                    Messages.USER_NOT_FOUND + forum.getUser()
            );
            return (ResponseEntity<Forum>) (Object) ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
        }
    }

    @Override
    public ResponseEntity<Forum> forumGetOne(String slug) {
        Forum bySlug = forumRepository.getBySlug(slug);

        if (bySlug == null) {
            MessageResponse messageResponse = new MessageResponse(
                    Messages.FORUM_NOT_FOUND + slug
            );

            return (ResponseEntity<Forum>) (Object) ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
        }
        return ResponseEntity.ok(bySlug);
    }

    @Override
    public ResponseEntity<Thread> threadCreate(String slug, Thread thread) {
        thread.setForum(slug);
        Thread result = threadRepository.create(thread);

        if (result != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } else {
            boolean existsByNickName = userRepository.existsBynickname(thread.getAuthor());

            if (!existsByNickName) {
                MessageResponse messageResponse = new MessageResponse(
                        Messages.USER_NOT_FOUND + thread.getAuthor()
                );

                return (ResponseEntity<Thread>) (Object) ResponseEntity
                        .status(HttpStatus.NOT_FOUND).body(messageResponse);
            }

            Thread bySlug = threadRepository.getBySlug(thread.getSlug());

            if (bySlug != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(bySlug);
            }

            MessageResponse messageResponse = new MessageResponse(
                    Messages.THREAD_FORUM_NOT_FOUND + slug
            );

            return (ResponseEntity<Thread>) (Object) ResponseEntity
                    .status(HttpStatus.NOT_FOUND).body(messageResponse);
        }
    }

    @Override
    public ResponseEntity<List<Thread>> forumGetThreads(String slug, Integer limit, OffsetDateTime since, Boolean desc) {
        List<Thread> threads = threadRepository.getThreads(slug, limit, since, desc);

        if (threads.isEmpty()) {
            boolean existsForum = forumRepository.existsBySlug(slug);
            if (!existsForum) {
                MessageResponse messageResponse = new MessageResponse(
                        Messages.FORUM_NOT_FOUND + slug
                );
                return (ResponseEntity<List<Thread>>) (Object) ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);
            }
        }
        return ResponseEntity.ok(threads);
    }
}
