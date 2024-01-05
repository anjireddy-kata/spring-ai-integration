package com.techmonks.openai.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateImageRequest {
    private String prompt;
    private String size;
    private int n;
}
