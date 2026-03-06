package org.example;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Chunker {
    private static final Logger log = LoggerFactory.getLogger(Chunker.class);
    private final DocumentSplitter recursiveSplitter;
    private final DocumentSplitter fallbackSplitter;

    public Chunker() {
        this.recursiveSplitter = DocumentSplitters.recursive(500, 100);
        this.fallbackSplitter = new DocumentByCharacterSplitter(1000, 200);
    }

    public List<TextSegment> getChunks(List<Document> documents) {
        List<TextSegment> segments = recursiveSplitter.splitAll(documents);
        // If recursive returns nothing (e.g. empty docs or splitter behavior), use character splitter
        if (segments.isEmpty()) {
            for (Document doc : documents) {
                if (doc != null && doc.text() != null && !doc.text().isBlank()) {
                    segments.addAll(fallbackSplitter.split(doc));
                }
            }
        }
        // Last resort: one segment per document
        if (segments.isEmpty()) {
            for (Document doc : documents) {
                if (doc != null && doc.text() != null && !doc.text().isBlank()) {
                    segments.add(doc.toTextSegment());
                }
            }
        }
        System.out.println("No of segments from chunker: " + segments.size());
        return segments;
    }
}
