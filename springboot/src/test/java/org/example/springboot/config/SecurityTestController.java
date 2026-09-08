package org.example.springboot.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityTestController {

    @GetMapping({
        "/api/acupuncture/doctor-story/ping",
        "/api/acupuncture/copper-man/acupoints",
        "/api/acupuncture/xuewei/ping",
        "/api/acupuncture/zhenjiu-tools/ping",
        "/api/private/ping",
        "/api/admin/ping"
    })
    public ResponseEntity<Void> getPing() {
        return ResponseEntity.ok().header("X-Security-Test", "ok").build();
    }

    @PostMapping({
        "/api/acupuncture/doctor-story/ping",
        "/api/user/login"
    })
    public ResponseEntity<Void> postPing() {
        return ResponseEntity.ok().header("X-Security-Test", "ok").build();
    }
}
