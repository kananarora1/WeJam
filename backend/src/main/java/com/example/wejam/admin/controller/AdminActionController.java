package com.example.wejam.admin.controller;

import com.example.wejam.admin.dto.AdminActionPage;
import com.example.wejam.admin.service.AdminActionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "Admin")
@RestController
@RequestMapping("/api/v1/admin")
class AdminActionController {

    private final AdminActionService adminActionService;

    AdminActionController(AdminActionService adminActionService) {
        this.adminActionService = adminActionService;
    }

    /** Newest first. For the next page, pass the previous page's nextBefore + nextBeforeId. */
    @GetMapping("/actions")
    AdminActionPage adminActions(@RequestParam(required = false) Instant before,
                                 @RequestParam(required = false) UUID beforeId) {
        return adminActionService.page(before, beforeId);
    }
}
