package com.optiflow.shared.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    record HealthResponse(String status, String backend) {
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("ok", "java");
    }
}
