package io.plagov.rssfeed.view;

import io.plagov.rssfeed.domain.response.PostResponse;
import io.plagov.rssfeed.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@WebMvcTest(PostsView.class)
class PostViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    void redirectsAnonymousUserToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test")
    void canViewTableOfUnreadPosts() throws Exception {
        List<PostResponse> testPosts = List.of(createTestPost());
        when(postService.getUnreadPosts()).thenReturn(testPosts);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("posts", testPosts));
    }

    @Test
    @WithMockUser(username = "test")
    void userCanMarkPostAsRead() throws Exception {
        var post1 = new PostResponse(1, 1, "Post 1", "https://post1.com", false, LocalDateTime.now());
        var post2 = new PostResponse(2, 1, "Post 2", "https://post2.com", false, LocalDateTime.now());
        var initialPosts = List.of(post1, post2);

        when(postService.getUnreadPosts())
                .thenReturn(initialPosts)
                .thenReturn(List.of(post1));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", initialPosts));

        mockMvc.perform(post("/mark-as-read")
                        .with(csrf())
                        .param("id", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", List.of(post1)));
    }

    @Test
    @WithMockUser(username = "test")
    void shouldViewNavigationHeader() throws Exception {
        mockMvc.perform(get("/"))
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
