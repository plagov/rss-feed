package io.plagov.rssfeed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.domain.Blog;
import org.springframework.stereotype.Component;

@Component
public class BlogService {

    private final BlogDao blogDao;
    private final ObjectMapper objectMapper;

    public BlogService(BlogDao blogDao, ObjectMapper objectMapper) {
        this.blogDao = blogDao;
        this.objectMapper = objectMapper;
    }

    public Blog patchBlog(int blogId, JsonPatch patch) {
        var blog = blogDao.getBlog(blogId);
        var jsonNode = objectMapper.valueToTree(blog);
        JsonNode patchedJsonNode;
        Blog patchedBlog;
        try {
            patchedJsonNode = patch.apply(jsonNode);
            patchedBlog = objectMapper.treeToValue(patchedJsonNode, Blog.class);
        } catch (JsonPatchException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        blogDao.updateBlog(blogId, patchedBlog.name(), patchedBlog.feedUrl(), patchedBlog.isSubscribed());
        return patchedBlog;
    }
}
