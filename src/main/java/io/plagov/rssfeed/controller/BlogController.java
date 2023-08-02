package io.plagov.rssfeed.controller;

import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.domain.Blog;
import io.plagov.rssfeed.domain.request.NewBlog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/blogs")
public class BlogController {

    private final BlogDao blogDao;

    private final Logger logger = LoggerFactory.getLogger(BlogController.class);

    public BlogController(BlogDao blogDao) {
        this.blogDao = blogDao;
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

    @PutMapping("/{id}")
    public Blog updateBlog(@PathVariable int id, @RequestBody NewBlog blog) {
        logger.info("Update blog with id {}.", id);
        blogDao.updateBlogById(id, blog.name(), blog.feedUrl());
        return blogDao.getBlogById(id);
    }

    @GetMapping("/{id}")
    public Blog getBlogById(@PathVariable int id) {
        return blogDao.getBlogById(id);
    }
}
