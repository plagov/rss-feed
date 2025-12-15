package io.plagov.rssfeed.service;

import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.domain.Blog;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

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
        } catch (JsonPatchException | JacksonException e) {
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

    public void subscribeToNewBlog(String feedUrl, String name) {
        blogDao.addNewBlog(feedUrl, name);
    }
}
