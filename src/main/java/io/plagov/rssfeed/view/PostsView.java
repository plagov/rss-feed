package io.plagov.rssfeed.view;

import io.plagov.rssfeed.dao.PostDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PostsView {

    private final PostDao postDao;

    private final Logger logger = LoggerFactory.getLogger(PostsView.class);

    @Value("${ALLOWED_USER_EMAIL}")
    private String allowedUserEmail;

    public PostsView(PostDao postDao) {
        this.postDao = postDao;
    }

    @GetMapping("/")
    public String getMainView(@AuthenticationPrincipal OAuth2User principal, Model model) {
        var email = principal.getAttributes().get("email").toString();
        if (!email.equals(allowedUserEmail)) {
            return "not_allowed";
        }
        var posts = postDao.getAllUnreadPosts();
        model.addAttribute("posts", posts);
        return "index";
    }

    @PostMapping("/mark-as-read")
    public String markPostAsRead(@RequestParam String id) {
        postDao.markPostAsRead(Integer.parseInt(id));
        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
