package io.plagov.rssfeed.service;

import io.plagov.rssfeed.domain.response.AiPostEvaluation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;

    @Value("classpath:/prompts/post-filter.st")
    private Resource systemPromptResource;

    public AiService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public AiPostEvaluation evaluatePost(String blogText) {
        return chatClient.prompt()
                .system(systemPromptResource)
                .user(blogText)
                .call()
                .entity(AiPostEvaluation.class);
    }
}
