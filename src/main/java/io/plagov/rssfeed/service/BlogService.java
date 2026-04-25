package io.plagov.rssfeed.service;

import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.domain.Blog;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlogService {

    private final BlogDao blogDao;
    private final UserContextService userContextService;

    public BlogService(BlogDao blogDao, UserContextService userContextService) {
        this.blogDao = blogDao;
        this.userContextService = userContextService;
    }

    public List<Blog> getSubscribedBlogs() {
        return blogDao.getBlogsForUser(true, userContextService.getCurrentUserId());
    }

    public void unsubscribeFromBlog(int blogId) {
        var userId = userContextService.getCurrentUserId();
        var blogEntity = blogDao.getBlogForUser(blogId, userId);
        blogDao.updateBlogForUser(blogId, blogEntity.name(), blogEntity.feedUrl(), false, userId);
    }

    public void subscribeToNewBlog(String feedUrl, String name) {
        blogDao.addNewBlogForUser(feedUrl, name, userContextService.getCurrentUserId());
    }
}
