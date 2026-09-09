package com.qtm.dashboard.otp.service;

import com.qtm.dashboard.otp.config.TwilioVerifyProperties;
import com.qtm.dashboard.otp.dto.OtpPhoneVerificationCheckRequest;
import com.qtm.dashboard.otp.dto.OtpPhoneVerificationSendRequest;
import com.qtm.dashboard.otp.dto.OtpVerificationResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Service minimale per invio/verifica OTP nel dashboard. Comportamento compatibile con TENANTS-app.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserOtpService {

    private final TwilioVerifyClient twilioVerifyClient;
    private final TwilioVerifyProperties twilioVerifyProperties;

    public OtpVerificationResultResponse sendOtpForPhone(OtpPhoneVerificationSendRequest request) {
        if (request == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Payload OTP obbligatorio");
        }
        var target = OtpTargetResolver.resolvePhone(
                request.getPhoneNumber(),
                request.getChannel(),
                twilioVerifyProperties.getDefaultChannel()
        );
        var verification = twilioVerifyClient.startVerification(target.destination(), target.channel());
        log.info("[UserOtpService] OTP avviato phone={} channel={} status={}", maskDestination(target.destination()), target.channel(), verification.status());
        return toResponse(null, verification);
    }

    public OtpVerificationResultResponse checkOtpForPhone(OtpPhoneVerificationCheckRequest request) {
        if (request == null || request.getCode() == null || request.getCode().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Codice OTP obbligatorio");
        }
        var target = OtpTargetResolver.resolvePhone(
            request.getPhoneNumber(),
            request.getChannel(),
            twilioVerifyProperties.getDefaultChannel()
        );
        var verification = twilioVerifyClient.checkVerification(target.destination(), request.getCode().trim());
        log.info("[UserOtpService] OTP verificato phone={} status={} approved={}", maskDestination(target.destination()), verification.status(), isApproved(verification));
        return toResponse(null, verification);
    }

    private OtpVerificationResultResponse toResponse(Long userId, TwilioVerifyClient.TwilioVerificationResource verification) {
        return OtpVerificationResultResponse.builder()
                .userId(userId)
                .destination(maskDestination(verification.to()))
                .channel(verification.channel())
                .status(verification.status())
                .approved(isApproved(verification))
                .verificationSid(verification.sid())
                .build();
    }

    private boolean isApproved(TwilioVerifyClient.TwilioVerificationResource verification) {
        return "approved".equalsIgnoreCase(verification.status()) || Boolean.TRUE.equals(verification.valid());
    }

    private String maskDestination(String destination) {
        if (destination == null || destination.isBlank()) {
            return "";
        }
        String trimmed = destination.trim();
        if (trimmed.length() <= 4) {
            return "****";
        }
        return "***" + trimmed.substring(trimmed.length() - 4);
    }
}
