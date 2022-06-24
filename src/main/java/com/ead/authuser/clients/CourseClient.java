package com.ead.authuser.clients;

import com.ead.authuser.dtos.CourseDto;
import com.ead.authuser.dtos.ResponsePageDto;
import com.ead.authuser.services.UtilsService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class CourseClient {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UtilsService utilsService;
    @Value("${ead.api.url.course}")
    private String requestUriCourse;

    //Utilizando API Composition Pattern
    //@Retry(name = "retryInstance", fallbackMethod = "retryfallback")//retentativas de acordo com a config feita no application.yml, caso falhe após as 3 tentativas chama o método no fallbackmethod
    //@CircuitBreaker(name = "circuitbreakerInstance", fallbackMethod = "circuitBreakerfallback") //fallbackMethod rota alterantiva caso erro
    public Page<CourseDto> getAllCoursesByUser(UUID userId, Pageable pageable, String token){
        //List<CourseDto> searchResult = null;
        ResponseEntity<ResponsePageDto<CourseDto>> result = null;
        String url = requestUriCourse + this.utilsService.createUrl(userId, pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> requestEntity = new HttpEntity<>("parameters", headers);

        log.debug("Request URL: {}", url);
        log.info("Request URL: {}", url);

        ParameterizedTypeReference<ResponsePageDto<CourseDto>> responseType =
                  new ParameterizedTypeReference<ResponsePageDto<CourseDto>>() {};

        result = this.restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);
        //searchResult = result.getBody().getContent();
        log.debug("Response Number of Elements: {}", result.getBody().getTotalElements());

        log.info("Ending request /courses userID {}", userId);
        return result.getBody();
    }

    //métodos que retornam uma pagina vazia para não retornar o erro. Ideial seria ter um erro customizado e retornar
    public Page<CourseDto> circuitBreakerfallback(UUID userId, Pageable pageable, Throwable t){
        log.error("Inside circuit breaker fallback, cause {}", t.toString());
        List<CourseDto> searchResult = new ArrayList<>();
        return new PageImpl<>(searchResult);
    }

    public Page<CourseDto> retryfallback(UUID userId, Pageable pageable, Throwable t){
        log.error("Inside retry retryfallback, cause {}", t.toString());
        List<CourseDto> searchResult = new ArrayList<>();
        return new PageImpl<>(searchResult);
    }
}
