package io.plagov.rssfeed.controller;

import io.plagov.rssfeed.service.PostService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final PostService postService;

    public BlogController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/{id}/fetch-latest")
    public String fetchLatestPostsForBlog(@PathVariable int id) {
        CompletableFuture.runAsync(() -> postService.recordLatestPostsForBlog(id));
        return "Fetching latest posts task is triggered";
    }
}
