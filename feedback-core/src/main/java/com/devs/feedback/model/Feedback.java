package com.devs.feedback.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.Instant;

@Data
public class Feedback {
    private String id;
    private String descricao;
    private int nota;
    @JsonIgnore
    private Instant dataEnvio;
    private boolean urgente;
}