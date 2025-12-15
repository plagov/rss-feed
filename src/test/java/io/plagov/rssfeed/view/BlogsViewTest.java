package io.plagov.rssfeed.view;

import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.domain.Blog;
import io.plagov.rssfeed.service.BlogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@TestPropertySource(properties = "ALLOWED_USER_EMAIL = test@example.com")
@WebMvcTest(BlogsView.class)
class BlogsViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BlogDao blogDao;

    @MockitoBean
    private BlogService blogService;

    private static final String ALLOWED_EMAIL = "test@example.com";

    @Test
    void shouldSubscribeButton() throws Exception {
        mockMvc.perform(get("/blogs")
                .with(oauth2Login().attributes(attr -> attr.put("email", ALLOWED_EMAIL))))
                .andExpect(xpath("//a[@data-testid='subscribe-button']/@href")
                        .string("/blogs/subscribe"));
    }

    @Test
    void shouldOpenSubscribeToNewBlogView() throws Exception {
        mockMvc.perform(get("/blogs/subscribe")
                        .with(oauth2Login().attributes(attr -> attr.put("email", ALLOWED_EMAIL))))
                .andExpect(status().isOk())
                .andExpect(view().name("subscribe"))
                .andExpect(xpath("//input[@id='name']").exists())
                .andExpect(xpath("//input[@id='feedUrl']").exists())
                .andExpect(xpath("//button[@data-testid='subscribe-button']").exists());
    }

    @Test
    void shouldSubscribeToNewBlog() throws Exception {
        var blogName = "Test blog";
        var blogUrl = "https://example.com/feed";
        var blog = new Blog(1, blogName, blogUrl, true);

        mockMvc.perform(post("/blogs")
                        .with(csrf())
                        .with(oauth2Login().attributes(attr -> attr.put("email", ALLOWED_EMAIL)))
                .param("name", blogName)
                .param("feedUrl", blogUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/blogs"));

        when(blogService.getSubscribedBlogs()).thenReturn(List.of(blog));

        mockMvc.perform(get("/blogs")
                .with(oauth2Login().attributes(attr -> attr.put("email", ALLOWED_EMAIL))))
                .andExpect(status().isOk())
                .andExpect(xpath("//*[@data-testclass='blog-card']").exists())
                .andExpect(xpath("//*[@data-testclass='blog-name']").string(blogName))
                .andExpect(xpath("//*[@data-testclass='blog-url']").string(blogUrl));
    }
}
