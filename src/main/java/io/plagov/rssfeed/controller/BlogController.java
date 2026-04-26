package io.plagov.rssfeed.controller;

import io.plagov.rssfeed.domain.Blog;
import io.plagov.rssfeed.domain.request.NewBlog;
import io.plagov.rssfeed.service.BlogService;
import io.plagov.rssfeed.service.PostService;
import io.plagov.rssfeed.service.UserContextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final PostService postService;
    private final BlogService blogService;
    private final UserContextService userContextService;

    public BlogController(PostService postService, BlogService blogService, UserContextService userContextService) {
        this.postService = postService;
        this.blogService = blogService;
        this.userContextService = userContextService;
    }

    @GetMapping
    public ResponseEntity<List<Blog>> getBlogs() {
        var userId = userContextService.getCurrentUserId();
        return ResponseEntity.ok(blogService.getSubscribedBlogs(userId));
    }

    @PostMapping
    public ResponseEntity<Void> addBlog(@RequestBody NewBlog newBlog) {
        var userId = userContextService.getCurrentUserId();
        blogService.subscribeToNewBlog(newBlog, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unsubscribe")
    public ResponseEntity<Void> unsubscribeFromBlog(@PathVariable int id) {
        var userId = userContextService.getCurrentUserId();
        blogService.unsubscribeFromBlog(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/fetch-latest")
    public ResponseEntity<String> fetchLatestPostsForBlog(@PathVariable int id) {
        var userId = userContextService.getCurrentUserId();
        CompletableFuture.runAsync(() -> postService.recordLatestPostsForBlog(id, userId));
        return ResponseEntity.ok("Fetching latest posts task is triggered");
    }
}
