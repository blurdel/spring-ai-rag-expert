package com.blurdel.springai.bootstrap;

import com.blurdel.springai.config.VectorStoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class LoadVectorStore implements CommandLineRunner {

    @Autowired
    VectorStore vectorStore;

    @Autowired
    VectorStoreProperties vsProps;

    @Override
    public void run(String... args) throws Exception {
        log.info("Loading vector store");

        if (vectorStore.similaritySearch("Sportsman").isEmpty()) {
            log.info("Loading documents into vector store");

            vsProps.getDocumentsToLoad().forEach(doc -> {
                log.info("Loading doc: " + doc.getFilename());

                TikaDocumentReader docReader = new TikaDocumentReader(doc);
                List<Document> documents = docReader.get();

                TextSplitter splitter = new TokenTextSplitter();

                List<Document> splitDocs = splitter.apply(documents);

                vectorStore.add(splitDocs);
            });
        }
        log.info("vector store loaded");
    }
}
