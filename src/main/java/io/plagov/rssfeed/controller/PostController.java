package io.plagov.rssfeed.controller;

import io.plagov.rssfeed.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }
    
    @PostMapping("/fetch-latest")
    public ResponseEntity<String> fetchLatestPosts() {
        CompletableFuture.runAsync(postService::recordLatestBlogPosts);
        return ResponseEntity.ok("Fetching latest posts task is triggered");
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Void> cleanupReadPosts() {
        postService.deleteReadPostsOlderThan30Days();
        return ResponseEntity.ok().build();
    }
}
