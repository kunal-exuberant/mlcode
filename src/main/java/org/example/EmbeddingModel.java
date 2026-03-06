package org.example;

import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;

public class EmbeddingModel {
    // BAAI/bge-base-en-v1.5
    private final dev.langchain4j.model.embedding.EmbeddingModel model = new AllMiniLmL6V2EmbeddingModel();

    public dev.langchain4j.model.embedding.EmbeddingModel getEmbeddingModel() {
        return model;
    }
}
