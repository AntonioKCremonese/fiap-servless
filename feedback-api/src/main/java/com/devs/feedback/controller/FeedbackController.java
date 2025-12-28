package com.devs.feedback.controller;

import com.devs.feedback.model.Feedback;
import com.devs.feedback.service.FeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/avaliacao")
@Slf4j
public class FeedbackController {

    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Feedback feedback) {
        log.info("Received feedback: {}", feedback);
        service.process(feedback);
        return ResponseEntity.ok("OK");
    }

//    @PostMapping
//    public Mono<ResponseEntity<String>> create(@RequestBody Mono<Feedback> feedback) {
//        log.info("Received feedback: {}", feedback);
//        return feedback.doOnNext(service::process)
//                .map(f -> ResponseEntity.ok("OK"));
//    }
}
