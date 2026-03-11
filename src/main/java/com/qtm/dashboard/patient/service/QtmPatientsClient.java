package com.qtm.dashboard.patient.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtm.commonlib.dto.PatientDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

/**
 * Client REST che inoltra le operazioni pazienti di QTMDashboard al servizio QTMPatients,
 * preservando gli header di sicurezza della richiesta corrente.
 */
@Service
public class QtmPatientsClient {

    private static final ParameterizedTypeReference<List<PatientDto>> PATIENT_LIST_TYPE = new ParameterizedTypeReference<>() {
    };
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final String SERVICE_UNAVAILABLE_MESSAGE = "Servizio QTMPatients non disponibile";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public QtmPatientsClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.patients.api-base-url}") String patientsApiBaseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(patientsApiBaseUrl).build();
        this.objectMapper = objectMapper;
    }

    public PatientDto create(PatientDto patientDto) {
        return execute(() -> restClient.post().uri("/patients")
                .headers(this::applyForwardedHeaders)
                .body(patientDto)
                .retrieve()
                .body(PatientDto.class));
    }

    public List<PatientDto> findAll() {
        return execute(() -> restClient.get().uri("/patients")
                .headers(this::applyForwardedHeaders)
                .retrieve()
                .body(PATIENT_LIST_TYPE));
    }

    public PatientDto findById(Long id) {
        return execute(() -> restClient.get().uri("/patients/{id}", id)
                .headers(this::applyForwardedHeaders)
                .retrieve()
                .body(PatientDto.class));
    }

    public PatientDto update(Long id, PatientDto patientDto) {
        return execute(() -> restClient.put().uri("/patients/{id}", id)
                .headers(this::applyForwardedHeaders)
                .body(patientDto)
                .retrieve()
                .body(PatientDto.class));
    }

    public void delete(Long id) {
        executeVoid(() -> restClient.delete().uri("/patients/{id}", id)
                .headers(this::applyForwardedHeaders)
                .retrieve()
                .toBodilessEntity());
    }

    private void applyForwardedHeaders(HttpHeaders headers) {
        HttpServletRequest currentRequest = resolveCurrentRequest();
        if (currentRequest == null) {
            return;
        }

        copyHeaderIfPresent(currentRequest, headers, HttpHeaders.AUTHORIZATION);
        copyHeaderIfPresent(currentRequest, headers, "X-Selected-Role");
        copyHeaderIfPresent(currentRequest, headers, "X-Selected-Client");
    }

    private void copyHeaderIfPresent(HttpServletRequest request, HttpHeaders headers, String headerName) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isBlank()) {
            headers.set(headerName, value);
        }
    }

    private HttpServletRequest resolveCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }
        return servletRequestAttributes.getRequest();
    }

    private <T> T execute(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (HttpStatusCodeException exception) {
            throw mapRemoteException(exception);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE, exception);
        }
    }

    private void executeVoid(Runnable operation) {
        try {
            operation.run();
        } catch (HttpStatusCodeException exception) {
            throw mapRemoteException(exception);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE, exception);
        }
    }

    private ResponseStatusException mapRemoteException(HttpStatusCodeException exception) {
        return new ResponseStatusException(exception.getStatusCode(), extractDetail(exception), exception);
    }

    private String extractDetail(HttpStatusCodeException exception) {
        String responseBody = exception.getResponseBodyAsString();
        if (responseBody == null || responseBody.isBlank()) {
            return defaultMessage(exception.getStatusCode().value());
        }

        try {
            Map<String, Object> body = objectMapper.readValue(responseBody, MAP_TYPE);
            Object detail = body.get("detail");
            if (detail instanceof String detailText && !detailText.isBlank()) {
                return detailText;
            }

            Object message = body.get("message");
            if (message instanceof String messageText && !messageText.isBlank()) {
                return messageText;
            }
        } catch (IOException ignored) {
            return responseBody;
        }

        return responseBody;
    }

    private String defaultMessage(int statusCode) {
        return HttpStatus.resolve(statusCode) == null
                ? SERVICE_UNAVAILABLE_MESSAGE
                : HttpStatus.resolve(statusCode).getReasonPhrase();
    }
}