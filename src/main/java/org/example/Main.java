package org.example;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        EnvLoader.load(".env");
        System.out.print("Hello RAG, Welcome!");

        RagApp ragApp = new RagApp();
        List<Document> documents;
        try {
            documents = ragApp.getDocuments();
        } catch (IOException e) {
            System.err.println("Failed to load documents from docs/: " + e.getMessage());
            return;
        }

        System.out.println("No of documents to ingest are: "+documents.size());

        Chunker chunker = new Chunker();
        List<TextSegment> segments = chunker.getChunks(documents);
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        EmbeddingModel embeddingModel = new EmbeddingModel();
        VectorDatabase vectorDatabase = new VectorDatabase(embeddingModel.getEmbeddingModel(), embeddingStore);
        if(segments.isEmpty()) {
            System.out.println("No Segments found by chunker");
            return;
        }
        vectorDatabase.ingest(segments);
        Scanner scanner = new Scanner(System.in);

        LLM llm = new LLM();

        ContentRetriever retriever = new ContentRetriever(embeddingStore, embeddingModel.getEmbeddingModel());

        while (true) {
            System.out.print("\nAsk> ");
            String question = scanner.nextLine();

            if (question.equalsIgnoreCase("exit")) break;

            List<Content> contents = retriever.retrieve(question);
            String context = contents.stream()
                    .map(Content::textSegment)
                    .map(TextSegment::text)
                    .collect(Collectors.joining("\n"));

            if (context.isBlank()) {
                System.out.println("\n(No relevant context found in documents. Answer may be generic.)");
            }

            String prompt = """
            You are a helpful assistant. Answer the question using ONLY the Context below.
            If the Context does not contain enough information, say "I don't have enough information in the provided context."
            Add: “Mention which part of the context supports your answer (e.g. ‘According to the document…’).”

            Context:
            %s

            Question:
            %s
            """.formatted(context.isBlank() ? "(none)" : context, question);

            String answer = llm.generate(prompt);
            System.out.println("\nAnswer:\n" + answer);
        }
    }
}