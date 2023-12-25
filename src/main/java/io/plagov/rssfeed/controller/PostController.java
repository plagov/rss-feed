package io.plagov.rssfeed.controller;

import io.plagov.rssfeed.dao.PostDao;
import io.plagov.rssfeed.domain.response.PostResponse;
import io.plagov.rssfeed.service.PostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/posts")
public class PostController {
    
    private final PostDao postDao;
    private final PostService postService;

    public PostController(PostDao postDao, PostService postService) {
        this.postDao = postDao;
        this.postService = postService;
    }
    
    @GetMapping("/unread")
    public List<PostResponse> getAllUnreadPosts() {
        return postDao.getAllUnreadPosts();
    }

    /**
     * @deprecated
     * This controller is replaced by a View Controller.
     * <p>See {@link io.plagov.rssfeed.view.PostsView#markPostAsRead}</p>
     */
    @Deprecated
    @PatchMapping("/mark-as-read/{id}")
    public void markPostAsRead(@PathVariable("id") int postId) {
        postDao.markPostAsRead(postId);
    }

    @PostMapping("/fetch-latest")
    public String fetchLatestPosts() {
        CompletableFuture.runAsync(postService::recordLatestBlogPosts);
        return "Fetching latest posts task is triggered";
    }
}
