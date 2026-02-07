# Monetisation RAG Chat

A RAG (Retrieval-Augmented Generation) chat application built with [LangChain4j](https://docs.langchain4j.dev/). It uses an in-memory vector store, in-process embeddings (AllMiniLmL6V2, no API key), and Google Gemini for answers.

## Prerequisites

- **Java 21**
- **Maven 3.6+**

## Setup

### 1. Clone and build

```bash
cd MonetisationRAGChat
mvn clean compile
```

### 2. Environment variables

Create a `.env` file in the project root (or export in your shell) with:

| Variable         | Description                   |
|------------------|-------------------------------|
| `GEMINI_API_KEY` | Google AI API key for Gemini  |

Embeddings run in-process (AllMiniLmL6V2), so no HuggingFace API key is needed.

Get a Gemini key: [Google AI Studio](https://ai.google.dev/gemini-api/docs/api-key)

Load env before running (example with `export`):

```bash
export GEMINI_API_KEY=your_gemini_key
```

## Run

```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

Or run `org.example.Main` from your IDE.

- Type your question at the `Ask>` prompt.
- Type **exit** to quit.

## Project structure

| Component        | Role |
|------------------|------|
| `Main`           | Entry point; REPL that retrieves context and calls the LLM. |
| `RagApp`         | Loads documents from the `docs/` directory. |
| `Chunker`        | Splits documents into segments (recursive splitter, 500 chars, 100 overlap). |
| `VectorDatabase` | Embeds segments and stores them in the embedding store. |
| `ContentRetriever` | Retrieves relevant content for a query from the store. |
| `EmbeddingModel` | In-process embedding model (AllMiniLmL6V2, same as MiniLM-L6-v2). |
| `LLM`            | Google Gemini chat model (`gemini-1.5-flash`). |

## Improving the app

See **[ENHANCEMENTS.md](ENHANCEMENTS.md)** for a structured list of improvements (more data, chunking, retrieval, prompt, persistent store, observability).

## Loading your own documents

1. Put text files under a **`docs/`** folder in the project root.
2. In code, use `RagApp` to load them, `Chunker` to split, and `VectorDatabase` to ingest into the same in-memory store you pass to `ContentRetriever` in `Main`. Until ingestion is wired in and run once, the store starts empty and retrieval may return no context.

## License

See repository defaults.
