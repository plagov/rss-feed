package io.plagov.rssfeed.controller;

import io.plagov.rssfeed.dao.PostDao;
import io.plagov.rssfeed.domain.response.PostResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {
    
    private final PostDao postDao;

    public PostController(PostDao postDao) {
        this.postDao = postDao;
    }
    
    @GetMapping("/unread")
    public List<PostResponse> getAllUnreadPosts() {
        return postDao.getAllUnreadPosts();
    }
}
