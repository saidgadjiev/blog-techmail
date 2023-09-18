package ru.gadjini.blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.gadjini.blog.common.Messages;
import ru.gadjini.blog.controller.ThreadApiDelegate;
import ru.gadjini.blog.dao.PostRepository;
import ru.gadjini.blog.dao.ThreadRepository;
import ru.gadjini.blog.dao.VoteRepository;
import ru.gadjini.blog.model.*;
import ru.gadjini.blog.model.Thread;

import java.util.List;

@Service
public class ThreadService implements ThreadApiDelegate {

    private final PostRepository postRepository;

    private final VoteRepository voteRepository;

    private final ThreadRepository threadRepository;

    @Autowired
    public ThreadService(PostRepository postRepository,
                         VoteRepository voteRepository,
                         ThreadRepository threadRepository) {
        this.postRepository = postRepository;
        this.voteRepository = voteRepository;
        this.threadRepository = threadRepository;
    }

    @Override
    public ResponseEntity<List<Post>> threadGetPosts(String slugOrId, Integer limit,
                                                     Long since, String sort, Boolean desc) {
        List<Post> posts = postRepository.getPosts(slugOrId, limit, since, sort, desc);
        if (posts.isEmpty()) {
            Boolean existsBySlugOrId = threadRepository.isExistsBySlugOrId(slugOrId);
            if (!existsBySlugOrId) {
                MessageResponse messageResponse = new MessageResponse(
                        Messages.THREAD_NOT_FOUND + slugOrId
                );
                return (ResponseEntity<List<Post>>) (Object) ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(messageResponse);
            }
        }

        return ResponseEntity.ok(posts);
    }

    @Override
    public ResponseEntity<Thread> threadGetOne(String slugOrId) {
        Thread bySlugOrId = threadRepository.getBySlugOrId(slugOrId);

        if (bySlugOrId != null) {
            return ResponseEntity.ok(bySlugOrId);
        } else {
            MessageResponse messageResponse = new MessageResponse(
                    Messages.THREAD_NOT_FOUND + ": " + slugOrId
            );

            return (ResponseEntity<Thread>) (Object) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(messageResponse);
        }
    }

    @Override
    public ResponseEntity<Thread> threadUpdate(String slugOrId, ThreadUpdate thread) {
        boolean updateThread = threadRepository.updateThread(slugOrId, thread);
        if (updateThread) {
            Thread bySlugOrId = threadRepository.getBySlugOrId(slugOrId);
            return ResponseEntity.ok(bySlugOrId);
        } else {
            MessageResponse messageResponse = new MessageResponse(
                    Messages.THREAD_NOT_FOUND + ": " + slugOrId
            );

            return (ResponseEntity<Thread>) (Object) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(messageResponse);
        }
    }

    @Override
    public ResponseEntity<Thread> threadVote(String slugOrId, Vote vote) {
        boolean created = voteRepository.create(slugOrId, vote);

        if (created) {
            Thread bySlugOrId = threadRepository.getBySlugOrId(slugOrId);
            return ResponseEntity.ok(bySlugOrId);
        } else {
            MessageResponse messageResponse = new MessageResponse(
                    Messages.THREAD_NOT_FOUND + slugOrId
            );

            return (ResponseEntity<Thread>) (Object) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(messageResponse);
        }
    }

    @Override
    public ResponseEntity<List<Post>> postsCreate(String slugOrId, List<Post> posts) {
        if (posts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(List.of());
        }
        postRepository.create(slugOrId, posts);

        return ResponseEntity.status(HttpStatus.CREATED).body(posts);
    }
}
