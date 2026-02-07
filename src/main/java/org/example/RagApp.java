package org.example;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RagApp {
    private static final ApachePdfBoxDocumentParser PDF_PARSER = new ApachePdfBoxDocumentParser();

    public List<Document> getDocuments() throws IOException {
        Path docsDir = Paths.get("docs").toAbsolutePath().normalize();
        List<Document> documents = new ArrayList<>();
        if (!Files.isDirectory(docsDir)) {
            return documents;
        }
        // Load each PDF explicitly (PathMatcher with loadDocuments can fail to find files)
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(docsDir, "*.pdf")) {
            for (Path path : stream) {
                Document doc = FileSystemDocumentLoader.loadDocument(path, PDF_PARSER);
                if (doc != null && doc.text() != null && !doc.text().isBlank()) {
                    documents.add(doc);
                }
            }
        }
        // Load each .txt file
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(docsDir, "*.txt")) {
            for (Path path : stream) {
                Document doc = FileSystemDocumentLoader.loadDocument(path);
                if (doc != null && doc.text() != null && !doc.text().isBlank()) {
                    documents.add(doc);
                }
            }
        }
        return documents;
    }
}
