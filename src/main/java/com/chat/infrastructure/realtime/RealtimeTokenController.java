package com.chat.infrastructure.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chat.api.RealtimeApi;
import com.chat.infrastructure.realtime.service.RealtimeService;
import com.chat.model.RealtimeTokenDTO;
import com.chat.security.guard.MemberGuard;

@RestController
@RequestMapping("/api")
@MemberGuard
@RequiredArgsConstructor
public class RealtimeTokenController implements RealtimeApi {
    private final RealtimeService realtimeService;

    @Override
    public RealtimeTokenDTO generateConnectionToken() {
        return new RealtimeTokenDTO(realtimeService.generateConnectionToken());
    }

    @Override
    public RealtimeTokenDTO generateSubscriptionToken(Long roomId) {
        return new RealtimeTokenDTO(realtimeService.generateSubscriptionToken(roomId));
    }
}
