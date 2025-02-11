package com.blurdel.springai.services;

import com.blurdel.springai.model.Answer;
import com.blurdel.springai.model.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class OpenAIServiceImpl implements OpenAIService {

    final ChatModel chatModel;
    final VectorStore vectorStore;

    @Value("classpath:/templates/rag-prompt-template.st")
    private Resource ragPromptTemplate;

    @Value("classpath:/templates/system-message.st")
    private Resource systemMessageTemplate;


    @Override
    public Answer getAnswer(Question question) {
        PromptTemplate sysMsgPT = new SystemPromptTemplate(systemMessageTemplate);
        Message sysMsg = sysMsgPT.createMessage();

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder().query(question.question()).topK(6).build());
        List<String> contentList = documents.stream().map(Document::getContent).toList();

        PromptTemplate pt = new PromptTemplate(ragPromptTemplate);
        Message userMsg = pt.createMessage(Map.of(
                "input", question.question(),
                "documents", String.join("\n", contentList)
            ));

//        contentList.forEach(System.out::println);

        ChatResponse resp = chatModel.call(new Prompt(List.of(sysMsg, userMsg)));

        return new Answer(resp.getResult().getOutput().getContent());
    }

}
