package io.plagov.rssfeed.service;

import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.domain.Blog;
import io.plagov.rssfeed.domain.request.NewBlog;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class BlogService {

    private final BlogDao blogDao;

    public BlogService(BlogDao blogDao) {
        this.blogDao = blogDao;
    }

    public List<Blog> getSubscribedBlogs(UUID userId) {
        return blogDao.getBlogsForUser(true, userId);
    }

    public void unsubscribeFromBlog(int blogId, UUID userId) {
        var blogEntity = blogDao.getBlogForUser(blogId, userId);
        blogDao.updateBlogForUser(blogId, blogEntity.name(), blogEntity.feedUrl(), false, userId);
    }

    public void subscribeToNewBlog(NewBlog newBlog, UUID userId) {
        blogDao.addNewBlogForUser(newBlog.feedUrl(), newBlog.name(), userId);
    }
}
