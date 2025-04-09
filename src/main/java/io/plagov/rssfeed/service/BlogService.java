package io.plagov.rssfeed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.domain.Blog;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

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

    public List<Blog> getSubscribedBlogs() {
        return blogDao.getBlogs(true);
    }

    public void unsubscribeFromBlog(int blogId) {
        ObjectNode patchOperation = objectMapper.createObjectNode()
                .put("op", "replace")
                .put("path", "/isSubscribed")
                .put("value", false);
        var patchArray = objectMapper.createArrayNode().add(patchOperation);
        JsonPatch jsonPatch;
        try {
            jsonPatch = JsonPatch.fromJson(patchArray);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a JSONPatch out of the JSON Node.", e);
        }

        patchBlog(blogId, jsonPatch);
    }
}
