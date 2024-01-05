package com.techmonks.openai.model;

import lombok.Getter;

import java.util.List;

@Getter
public class GeneratedImageResponse {
    private List<GeneratedImage> data;
}
