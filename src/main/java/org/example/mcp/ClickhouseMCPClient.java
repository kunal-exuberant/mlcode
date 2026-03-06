package org.example.mcp;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;

import java.util.List;
import java.util.Map;

public class ClickhouseMCPClient {
    public McpClient getClickHouseClient() {
        return clickHouseClient;
    }

    McpClient clickHouseClient;

    public ClickhouseMCPClient() {

        this.clickHouseClient = new DefaultMcpClient.Builder()
                .transport(new StdioMcpTransport.Builder()
                        // Command to run the official ClickHouse MCP server
                        .command(List.of("uv", "run", "--with", "mcp-clickhouse", "mcp-clickhouse"))
                        .environment(Map.of(
                                "CLICKHOUSE_HOST", "localhost",
                                "CLICKHOUSE_PORT", "8123", // Default HTTP port for ClickHouse
                                "CLICKHOUSE_USER", "default",
                                "CLICKHOUSE_PASSWORD", "",
                                "CLICKHOUSE_SECURE", "false" // Set to true if using SSL/TLS
                        ))
                        .build())
                .build();
    }
}
