package com.example.eventmanagement.controller;

import com.example.eventmanagement.exception.DuplicateEmailException;
import com.example.eventmanagement.exception.DuplicateUsernameException;
import com.example.eventmanagement.model.User;
import com.example.eventmanagement.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(name = "registerRole", defaultValue = "user") String registerRole,
            Model model,
            RedirectAttributes redirectAttributes) {

        String grantedRole = "admin".equals(registerRole) ? "ROLE_ADMIN" : "ROLE_USER";

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        try {
            userService.registerUser(user, grantedRole);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (DuplicateUsernameException e) {
            User u = new User();
            u.setUsername(username);
            u.setEmail(email);
            model.addAttribute("user", u);
            model.addAttribute("selectedRole", registerRole);
            model.addAttribute("error", "Username already taken");
            return "register";
        } catch (DuplicateEmailException e) {
            User u = new User();
            u.setUsername(username);
            u.setEmail(email);
            model.addAttribute("user", u);
            model.addAttribute("selectedRole", registerRole);
            model.addAttribute("error", "Email already registered");
            return "register";
        }
    }
}
