package io.plagov.rssfeed.controller;

import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.domain.Blog;
import io.plagov.rssfeed.domain.request.NewBlog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blogs")
public class BlogController {

    private final BlogDao blogDao;

    public BlogController(BlogDao blogDao) {
        this.blogDao = blogDao;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addNewBlog(@RequestBody NewBlog blog) {
        blogDao.addNewBlog(blog.name(), blog.feedUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body("Blog %s added successfully".formatted(blog.name()));
    }

    @GetMapping("/all")
    public List<Blog> getAllBlogs() {
        return blogDao.getAllBlogs();
    }
}
