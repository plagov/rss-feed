package io.plagov.rssfeed.controller;

import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.domain.request.NewBlog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
