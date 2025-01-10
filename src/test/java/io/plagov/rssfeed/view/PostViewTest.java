package io.plagov.rssfeed.view;

import io.plagov.rssfeed.dao.PostDao;
import io.plagov.rssfeed.domain.response.PostResponse;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource(properties = "ALLOWED_USER_EMAIL = test@example.com")
@WebMvcTest(PostsView.class)
class PostViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostDao postDao;

    private static final String ALLOWED_EMAIL = "test@example.com";

    @Test
    void shouldThrownNotAllowed_whenUsingNotAllowedEmail() throws Exception {
        var notAllowedEmail = "not@allowed.com";

        mockMvc.perform(get("/")
                        .with(oauth2Login().attributes(attr -> attr.put("email", notAllowedEmail))))
                .andExpect(status().isOk())
                .andExpect(view().name("not_allowed"))
                .andExpect(xpath("//body/div/p").string("You are not allowed to access this website!"));
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
    void canViewCorrectNumberOfColumns() throws Exception {
        List<PostResponse> testPosts = List.of(createTestPost());
        when(postDao.getAllUnreadPosts()).thenReturn(testPosts);

        mockMvc.perform(get("/")
                        .with(oauth2Login().attributes(attr -> attr.put("email", ALLOWED_EMAIL))))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(xpath("//table/thead/tr/th[1]").string("ID"))
                .andExpect(xpath("//table/thead/tr/th[2]").string("Title"))
                .andExpect(xpath("//table/thead/tr/th[3]").string("Date added"))
                .andExpect(xpath("//table/thead/tr/th[4]").string("Mark as read"));
    }

    @Test
    void titleColumnContainsLinks() throws Exception {
        List<PostResponse> testPosts = List.of(createTestPost());
        when(postDao.getAllUnreadPosts()).thenReturn(testPosts);

        mockMvc.perform(get("/")
                        .with(oauth2Login().attributes(attr -> attr.put("email", ALLOWED_EMAIL))))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(xpath("//tbody/tr[1]/td[2]/a/@href").string("https://post1.com"));
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
