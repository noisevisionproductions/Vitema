package integration.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class TestAdminController {

    @GetMapping("/login")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAdminResource() {
        return "Admin resource";
    }
}