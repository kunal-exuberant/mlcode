package org.example;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.example.mcp.CalculatorService;
import org.example.mcp.ClickhouseMCPClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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

/*        while (true) {
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
        }*/


        /*            McpClient mcpClient = new DefaultMcpClient.Builder()
                    .transport(new StdioMcpTransport.Builder()
                            .command(List.of("npx", "-y", "@f4ww4z/mcp-mysql-server",
                                    "mysql://root:root@localhost:3306/learning"))
                            .build())
                    .build();*/


        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(new StdioMcpTransport.Builder()
                        .command(List.of("npx", "-y", "@benborla29/mcp-server-mysql"))
                        .environment(Map.of(
                                "MYSQL_HOST", "localhost",
                                "MYSQL_PORT", "3306",
                                "MYSQL_USER", "root",
                                "MYSQL_PASS", "root",
                                "MYSQL_DB", "learning",
                                "ALLOW_INSERT_OPERATION", "true",
                                "ALLOW_UPDATE_OPERATION", "true"
                        ))
                        .logEvents(true)
                        .build())
                .build();

        ClickhouseMCPClient clickhouseMCPClient = new ClickhouseMCPClient();

        // McpToolProvider supplies both tool specs and execution (so the agent actually runs tools on the MCP servers)
        McpToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClient)
                .mcpClients(clickhouseMCPClient.getClickHouseClient())
                .build();
        try {
            var mysqlTools = mcpClient.listTools();
            System.out.println("MySQL MCP tools: " + mysqlTools.size());
            mysqlTools.forEach(tool -> System.out.println("  Tool: " + tool.name()));
        } catch (Exception e) {
            System.err.println("MySQL MCP server unavailable (is MySQL running on localhost:3306?): " + e.getMessage());
        }
        try {
            var chTools = clickhouseMCPClient.getClickHouseClient().listTools();
            System.out.println("ClickHouse MCP tools: " + chTools.size());
            chTools.forEach(tool -> System.out.println("  Tool: " + tool.name()));
        } catch (Exception e) {
            System.err.println("ClickHouse MCP server unavailable (is ClickHouse running? uv/mcp-clickhouse installed?): " + e.getMessage());
        }

        Agent agent = AiServices.builder(Agent.class)
                .chatLanguageModel(llm.getChatModel())
                .toolProvider(toolProvider)
                .build();
        //callAgent(agent);

        // Be explicit: the only tool is "mysql_query". To list tables, call it with SQL "SHOW TABLES".
        String systemHint = "You are a MySQL database assistant. You have exactly one tool: mysql_query (it runs SQL). " +
                "To show tables, you MUST call mysql_query with the SQL string: SHOW TABLES. " +
                "Do not invent other tool names like show_tables(). Call the mysql_query tool with the appropriate SQL.";
        String userQuestion = "Show me the tables in my database";
        String response = agent.answer(systemHint + "\n\nUser request: " + userQuestion);
        System.out.println(response);

        userQuestion = "Summaries the table";
        response = agent.answer(systemHint + "\n\nUser request: " + userQuestion);

        System.out.println(response);

        userQuestion = "select the product id which has the least price";
        response = agent.answer(systemHint + "\n\nUser request: " + userQuestion);

        System.out.println(response);

    }

    public static void callAgent(Agent agent){
        // Interact with the agent
        String userMessage1 = "What is 15 * 23 + 7?";
        System.out.println("User: " + userMessage1);
        String agentResponse1 = agent.answer(userMessage1);
        System.out.println("Agent: " + agentResponse1); // The agent should use the multiply and add tools

        String userMessage2 = "Explain the concept of agentic AI.";
        System.out.println("\nUser: " + userMessage2);
        String agentResponse2 = agent.answer(userMessage2);
        System.out.println("Agent: " + agentResponse2); // The agent answers directly without tools
    }
}