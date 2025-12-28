package com.devs.feedback;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.function.Function;

@Configuration
public class HelloFunction {

    @Bean
    public Function<Map<String, Object>, Map<String, Object>> hello() {
        return input -> Map.of(
                "message", "Lambda Spring Boot funcionando 🎉",
                "input", input
        );
    }
}
