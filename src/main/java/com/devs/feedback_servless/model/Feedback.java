package com.devs.feedback_servless.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;

@Data
public class Feedback {
    private String id;
    private String descricao;
    private int nota;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant dataEnvio;
    private boolean urgente;
}
