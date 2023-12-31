package com.techmonks.openai.controller;

import com.techmonks.openai.model.TranslationModel;
import com.techmonks.openai.model.TranslationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.ai.prompt.PromptTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("v1")
public class OpenAiController {
    private final ChatClient chatClient;

    @PostMapping("translations")
    public ResponseEntity<TranslationResponse> translate(@RequestBody TranslationModel translationModel) {
        BeanOutputParser<TranslationResponse> beanOutputParser = new BeanOutputParser<>(TranslationResponse.class);
        PromptTemplate promptTemplate = new PromptTemplate("""
                Translate the following {fromLanguage} text to {toLanguage}: {textToTranslate} {format}""");
        promptTemplate.add("fromLanguage", translationModel.getTranslateFrom());
        promptTemplate.add("toLanguage", translationModel.getTranslateTo());
        promptTemplate.add("textToTranslate", translationModel.getTextToTranslate());

        promptTemplate.add("format", beanOutputParser.getFormat());
        promptTemplate.setOutputParser(beanOutputParser);

        ChatResponse chatResponse = this.chatClient.generate(promptTemplate.create());
        return ResponseEntity.ok(beanOutputParser.parse(chatResponse.getGeneration().getContent()));
    }

}
