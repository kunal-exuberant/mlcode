# How to Use the MCP Server and Client

Your app runs both an **MCP server** (exposes tools over HTTP) and an **MCP client** (calls those tools via an LLM). Here’s how to use them.

---

## 1. What’s running

When you start the **web app** (`mvn spring-boot:run` or run `WebApplication`):

- **MCP server** – Listens on the same port as the web app (8080). It exposes **tools** (e.g. weather) over the [Model Context Protocol](https://modelcontextprotocol.io/).
- **MCP client** – Configured in `application.properties` to connect to `http://localhost:8080`, i.e. to your own MCP server.

So in one process you have:

- Server: “here are my tools (e.g. get temperature).”
- Client: “I connect to that server and let the LLM call those tools when answering.”

---

## 2. MCP server endpoints

The MCP WebMVC transport uses:

- **SSE endpoint** – `http://localhost:8080/sse` (default)
- **Message endpoint** – `http://localhost:8080/mcp/message` (default)

Other MCP clients (e.g. Cursor, another app) can point to `http://localhost:8080` and use these to list and call tools.

---

## 3. Tools exposed by the server

- **WeatherService** (in `org.example.mcp`) – Exposes a tool to get the current temperature for a city (e.g. “What’s the weather in Paris?”). Right now it returns a fixed value; you can replace it with a real API.

The server auto-configuration picks up tools from your Spring beans and exposes them via MCP.

---

## 4. Using the MCP client (chat with tools)

The **client** uses an LLM (OpenAI in this setup) and the tools from the server to answer questions.

### Step 1: Set your OpenAI API key

The MCP client needs a **ChatClient** (LLM). The web app reads **OPENAI_API_KEY** from your **.env** file (same as for Gemini) and enables the OpenAI chat model when the key is present.

1. **Add to your `.env` file** in the project root (same folder as `pom.xml`):

   ```
   OPENAI_API_KEY=sk-your-openai-key-here
   ```

   The web app loads `.env` at startup and passes this to Spring. No need to change `application.properties`.

2. **Restart the web app** (`mvn spring-boot:run` or run `WebApplication`). Ensure you run from the **project root** so `.env` is found.

### Step 2: Use the chat endpoint

Once the app starts with the API key and without excluding `OpenAiChatAutoConfiguration`:

- **POST** to **`/chat`** with a JSON body:

  ```json
  { "message": "What's the weather like in Paris?" }
  ```

  The LLM can then use the MCP server’s tools (e.g. get temperature) to answer. You’ll get a JSON response like:

  ```json
  { "response": "Current temperature in Paris: 22°C" }
  ```

### Step 3: Optional – demo at startup

If `ChatClient` is available, a **CommandLineRunner** runs once at startup and asks the same “What’s the weather like in Paris?” question. You’ll see the answer in the console. You can disable it by removing or commenting out the `demo` bean in `MCPClient.java`.

---

## 5. Summary

| Goal | What to do |
|------|------------|
| **Only serve HTTP and MCP server** | Run the web app as you do now. No OpenAI key needed. `GET /` and MCP endpoints work. |
| **Use MCP client (LLM + tools)** | Set `spring.ai.openai.api-key`, remove `OpenAiChatAutoConfiguration` from `exclude`, restart. Then use **POST /chat** with `{"message": "..."}`. |
| **Connect another MCP client to your server** | Point it to `http://localhost:8080` (or your host/port). Use the default paths `/sse` and `/mcp/message` if you didn’t change them. |

---

## 6. Two ways to “use” MCP

1. **From this app** – Use **POST /chat** so the **MCP client** in this app calls the **MCP server** (same app) and the LLM uses the tools to answer.
2. **From outside** – Run this app as the MCP server, and connect **Cursor**, **Claude Desktop**, or another MCP client to `http://localhost:8080` so they can use your tools (e.g. weather).

For more details on the protocol and options, see [Spring AI MCP](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html) and [Model Context Protocol](https://modelcontextprotocol.io/).
