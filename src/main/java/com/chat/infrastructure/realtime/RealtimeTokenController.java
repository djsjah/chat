package com.chat.infrastructure.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chat.model.RealtimeTokenDTO;
import com.chat.security.guard.MemberGuard;

@RestController
@RequestMapping("/api/realtime/token")
@MemberGuard
@RequiredArgsConstructor
public class RealtimeTokenController {
    private final RealtimeService realtimeService;

    @GetMapping("/connection")
    public RealtimeTokenDTO generateConnectionToken() {
        return new RealtimeTokenDTO(realtimeService.generateConnectionToken());
    }

    @GetMapping("/subscription/room/{roomId}")
    public RealtimeTokenDTO generateSubscriptionToken(@PathVariable Long roomId) {
        return new RealtimeTokenDTO(realtimeService.generateSubscriptionToken(roomId));
    }
}
