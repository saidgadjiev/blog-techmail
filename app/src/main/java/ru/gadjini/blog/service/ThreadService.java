package ru.gadjini.blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.gadjini.blog.controller.ThreadApiDelegate;
import ru.gadjini.blog.dao.PostRepository;
import ru.gadjini.blog.model.Post;

import java.util.List;

@Service
public class ThreadService implements ThreadApiDelegate {

    private final PostRepository postRepository;

    @Autowired
    public ThreadService(PostRepository postRepository) {
        this.postRepository = postRepository;
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
