package io.plagov.rssfeed.view;

import io.plagov.rssfeed.domain.request.RegisterRequest;
import io.plagov.rssfeed.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthView {

    private final AuthService authService;

    public AuthView(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("registrationOpen", authService.isRegistrationOpen());
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!authService.isRegistrationOpen()) {
            return "redirect:/login";
        }
        model.addAttribute("registrationOpen", true);
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false) String email,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            authService.registerUser(new RegisterRequest(username, password, email));
            redirectAttributes.addFlashAttribute("registered", true);
            return "redirect:/login";
        } catch (RuntimeException exception) {
            model.addAttribute("error", exception.getMessage());
            model.addAttribute("registrationOpen", authService.isRegistrationOpen());
            return "register";
        }
    }
}
