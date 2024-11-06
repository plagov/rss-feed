package io.plagov.rssfeed.view;

import io.plagov.rssfeed.service.ApiTokenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/tokens")
public class TokenView {

    private final ApiTokenService tokenService;

    public TokenView(ApiTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping
    public String showTokens(Model model) {
        model.addAttribute("tokens", tokenService.getAllTokens());
        return "tokens";
    }

    @PostMapping
    public String generateToken(@RequestParam String description) {
        tokenService.generateNewToken(description);
        return "redirect:/tokens";
    }
}
