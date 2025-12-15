package io.plagov.rssfeed.service;

import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.domain.Blog;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlogService {

    private final BlogDao blogDao;

    public BlogService(BlogDao blogDao) {
        this.blogDao = blogDao;
    }

    public List<Blog> getSubscribedBlogs() {
        return blogDao.getBlogs(true);
    }

    public void unsubscribeFromBlog(int blogId) {
        var blogEntity = blogDao.getBlog(blogId);
        blogDao.updateBlog(blogId, blogEntity.name(), blogEntity.feedUrl(), false);
    }

    public void subscribeToNewBlog(String feedUrl, String name) {
        blogDao.addNewBlog(feedUrl, name);
    }
}
