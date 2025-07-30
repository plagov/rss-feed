package io.plagov.rssfeed.view;

import io.plagov.rssfeed.dao.PostDao;
import io.plagov.rssfeed.domain.response.PostResponse;
import io.plagov.rssfeed.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@TestPropertySource(properties = "ALLOWED_USER_EMAIL = test@example.com")
@WebMvcTest(PostsView.class)
class PostViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostDao postDao;

    @MockitoBean
    private PostService postService;

    private static final String ALLOWED_EMAIL = "test@example.com";

    @Test
    void shouldThrownNotAllowed_whenUsingNotAllowedEmail() throws Exception {
        var notAllowedEmail = "not@allowed.com";

        mockMvc.perform(get("/")
                        .with(oauth2Login().attributes(attr -> attr.put("email", notAllowedEmail))))
                .andExpect(status().isOk())
                .andExpect(view().name("not_allowed"))
                .andExpect(xpath("//p[@data-testid='error-message']")
                        .string("You are not allowed to access this website!"));
    }

    @Test
    void canViewTableOfUnreadPosts() throws Exception {
        List<PostResponse> testPosts = List.of(createTestPost());
        when(postDao.getAllUnreadPosts()).thenReturn(testPosts);

        mockMvc.perform(get("/")
                        .with(oauth2Login().attributes(attr -> attr.put("email", ALLOWED_EMAIL))))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("posts", testPosts));
    }

    @Test
    void userCanMarkPostAsRead() throws Exception {
        var post1 = new PostResponse(1, 1, "Post 1", "https://post1.com", false, LocalDateTime.now());
        var post2 = new PostResponse(2, 1, "Post 2", "https://post2.com", false, LocalDateTime.now());
        var initialPosts = List.of(post1, post2);

        when(postDao.getAllUnreadPosts())
                .thenReturn(initialPosts)
                .thenReturn(List.of(post1));

        // Initial view
        mockMvc.perform(get("/")
                        .with(oauth2Login().attributes(attr -> attr.put("email", ALLOWED_EMAIL))))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", initialPosts));

        // Mark post2 as read
        mockMvc.perform(post("/mark-as-read")
                        .with(oauth2Login().attributes(attr -> attr.put("email", ALLOWED_EMAIL)))
                        .with(csrf())
                        .param("id", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verify only post1 remains
        mockMvc.perform(get("/")
                        .with(oauth2Login().attributes(attr -> attr.put("email", ALLOWED_EMAIL))))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", List.of(post1)));
    }

    @Test
    void shouldViewNavigationHeader() throws Exception {
        mockMvc.perform(get("/")
                .with(oauth2Login().attributes(attr -> attr.put("email", ALLOWED_EMAIL))))
                .andExpect(xpath("//a[@data-testid='posts']").string("Posts"))
                .andExpect(xpath("//a[@data-testid='posts']/@href").string("/"))
                .andExpect(xpath("//a[@data-testid='blogs']").string("Blogs"))
                .andExpect(xpath("//a[@data-testid='blogs']/@href").string("/blogs"))
                .andExpect(xpath("//a[@data-testid='tokens']").string("Tokens"))
                .andExpect(xpath("//a[@data-testid='tokens']/@href").string("/tokens"));
    }

    private PostResponse createTestPost() {
        return new PostResponse(
                1,
                1,
                "Test Post",
                "https://post1.com",
                false,
                LocalDateTime.now());
    }
}
