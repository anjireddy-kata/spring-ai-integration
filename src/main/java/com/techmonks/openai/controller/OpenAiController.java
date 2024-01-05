package com.techmonks.openai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmonks.openai.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.ai.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("v1")
public class OpenAiController {
    private final ChatClient chatClient;

    private final WebClient webClient;

    @Value("${spring.ai.openai.image-generator-url}")
    private String imageGeneratorUrl;
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

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

    @PostMapping("images")
    public ResponseEntity<GeneratedImageResponse> generateImages(@RequestBody ImageGeneratorRequest imageGeneratorRequest) throws JsonProcessingException {
        PromptTemplate promptTemplate = new PromptTemplate("""
                Please enhance the {promptText}
                 to make it more creative and fancy. Generated text length should not exceed 300 characters very strictly.""");
        //Please note that max prompt length is 1000 characters. Limiting the prompt length to avoid issues
        promptTemplate.add("promptText", imageGeneratorRequest.getPromptText());
        ChatResponse chatResponse = this.chatClient.generate(promptTemplate.create());
        String prompt = chatResponse.getGeneration().getContent();
        GenerateImageRequest generateImageRequest = new GenerateImageRequest();
        generateImageRequest.setPrompt(prompt);
        generateImageRequest.setSize(imageGeneratorRequest.getImageSize());
        generateImageRequest.setN(1);
        ObjectMapper objectMapper = new ObjectMapper();
        String imageJsonRequest = objectMapper.writeValueAsString(generateImageRequest);
        GeneratedImageResponse generatedImageResponse = getGeneratedImageResponse(imageJsonRequest);
        return ResponseEntity.ok(generatedImageResponse);

    }

    private GeneratedImageResponse getGeneratedImageResponse(String imageJsonRequest) {
        GeneratedImageResponse generatedImageResponse = webClient.post().uri(imageGeneratorUrl)
                .header("Authorization", "Bearer "+ apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(imageJsonRequest))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, res -> {
                    System.out.println(res);
                    return res.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new RuntimeException(body)));
                })
                .onStatus(HttpStatusCode::is5xxServerError, res -> {
                    System.out.println(res);
                    return res.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new RuntimeException(body)));
                })
                .bodyToMono(GeneratedImageResponse.class).block();
        return generatedImageResponse;
    }

}
