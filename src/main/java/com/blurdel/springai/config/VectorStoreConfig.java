package com.blurdel.springai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;

@Slf4j
@Configuration
public class VectorStoreConfig {

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel, VectorStoreProperties vsProps) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();

        File vectorStoreFile = new File(vsProps.getVectorStorePath());

        if (vectorStoreFile.exists()) {
            store.load(vectorStoreFile);
        }
        else {
            log.debug("Loading documents into vector store");
            vsProps.getDocumentsToLoad().forEach(doc -> {
                log.debug("Loading document: {}", doc.getFilename());
                TikaDocumentReader reader = new TikaDocumentReader(doc);
                List<Document> docs = reader.get();

                TextSplitter splitter = new TokenTextSplitter();
                List<Document> splitDocs = splitter.apply(docs);
                store.add(splitDocs);
            });

            store.save(vectorStoreFile);
        }

        return store;
    }

}
