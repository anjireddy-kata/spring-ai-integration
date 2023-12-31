package com.techmonks.openai.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TranslationModel {
    private String textToTranslate;
    private String translateFrom;
    private String translateTo;
}
