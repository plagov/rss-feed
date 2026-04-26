package io.plagov.rssfeed.controller;

import io.plagov.rssfeed.domain.response.PostResponse;
import io.plagov.rssfeed.service.PostService;
import io.plagov.rssfeed.service.UserContextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final UserContextService userContextService;

    public PostController(PostService postService, UserContextService userContextService) {
        this.postService = postService;
        this.userContextService = userContextService;
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getUnreadPosts() {
        var userId = userContextService.getCurrentUserId();
        return ResponseEntity.ok(postService.getUnreadPosts(userId));
    }

    @PostMapping("/{id}/mark-as-read")
    public ResponseEntity<Void> markPostAsRead(@PathVariable int id) {
        var userId = userContextService.getCurrentUserId();
        postService.markPostAsRead(id, userId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/fetch-latest")
    public ResponseEntity<String> fetchLatestPosts() {
        var userId = userContextService.getCurrentUserId();
        CompletableFuture.runAsync(() -> postService.recordLatestBlogPosts(userId));
        return ResponseEntity.ok("Fetching latest posts task is triggered");
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Void> cleanupReadPosts() {
        var userId = userContextService.getCurrentUserId();
        postService.deleteReadPostsOlderThan30Days(userId);
        return ResponseEntity.ok().build();
    }
}
