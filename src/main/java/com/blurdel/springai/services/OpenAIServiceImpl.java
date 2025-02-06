package com.blurdel.springai.services;

import com.blurdel.springai.model.Answer;
import com.blurdel.springai.model.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class OpenAIServiceImpl implements OpenAIService {

    private final ChatModel chatModel;
    private final SimpleVectorStore vectorStore;

    @Value("classpath:/templates/rag-prompt-template-meta.st")
    private Resource ragPromptTemplate;


    @Override
    public Answer getAnswer(Question question) {

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder().query(question.question()).topK(5).build());
        List<String> contentList = documents.stream().map(Document::getContent).toList();

        PromptTemplate pt = new PromptTemplate(ragPromptTemplate);
        Prompt prompt = pt.create(Map.of(
                "input", question.question(),
                "documents", String.join("\n", contentList)
            ));

        contentList.forEach(System.out::println);

        ChatResponse resp = chatModel.call(prompt);

        return new Answer(resp.getResult().getOutput().getContent());
    }

}
