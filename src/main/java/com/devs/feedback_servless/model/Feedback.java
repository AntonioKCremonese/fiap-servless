package com.devs.feedback_servless.model;

import lombok.Data;

import java.time.Instant;

@Data
public class Feedback {
    private String id;
    private String descricao;
    private int nota;
    private Instant dataEnvio;
    private boolean urgente;
}
