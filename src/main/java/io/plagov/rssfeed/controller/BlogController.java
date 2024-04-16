package io.plagov.rssfeed.controller;

import com.github.fge.jsonpatch.JsonPatch;
import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.domain.Blog;
import io.plagov.rssfeed.domain.request.NewBlog;
import io.plagov.rssfeed.service.BlogService;
import io.plagov.rssfeed.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/blogs")
public class BlogController {

    private final BlogDao blogDao;
    private final PostService postService;
    private final BlogService blogService;

    public BlogController(BlogDao blogDao,
                          PostService postService,
                          BlogService blogService) {
        this.blogDao = blogDao;
        this.postService = postService;
        this.blogService = blogService;
    }

    @PostMapping("/add")
    public ResponseEntity<Integer> addNewBlog(@RequestBody NewBlog blog) {
        var blogId = blogDao.addNewBlog(blog.name(), blog.feedUrl());
        return new ResponseEntity<>(blogId, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public List<Blog> getAllBlogs() {
        return blogDao.getAllBlogs();
    }

    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    public Blog updateBlog(@PathVariable int id, @RequestBody JsonPatch payload) {
        return blogService.patchBlog(id, payload);
    }

    @GetMapping("/{id}")
    public Blog getBlogById(@PathVariable int id) {
        return blogDao.getBlog(id);
    }

    @PostMapping("/{id}/fetch-latest")
    public String fetchLatestPostsForBlog(@PathVariable int id) {
        CompletableFuture.runAsync(() -> postService.recordLatestPostsForBlog(id));
        return "Fetching latest posts task is triggered";
    }
}
