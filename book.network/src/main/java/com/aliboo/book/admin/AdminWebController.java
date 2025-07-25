package com.aliboo.book.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin-panel")
public class AdminWebController {
    
    @GetMapping("/login")
    public String adminLogin() {
        return "admin/login";
    }
    
    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }
    
    @GetMapping("/users")
    public String adminUsers() {
        return "admin/users";
    }
}
