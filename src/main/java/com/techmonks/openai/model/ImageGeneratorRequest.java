package com.techmonks.openai.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ImageGeneratorRequest {
    private String promptText;
    private String imageSize;
}
