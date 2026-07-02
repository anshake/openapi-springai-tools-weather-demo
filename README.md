# openapi-springai-tools-weather-demo

A minimal, runnable demo of [openapi-spring-ai-tools](https://github.com/anshake/openapi-spring-ai-tools).

It points the library at the [Open-Meteo](https://open-meteo.com/) OpenAPI spec, turns
every operation into a Spring AI tool, and lets LLM answer a weather
question by calling the real API — with no per-endpoint code.

## What it does

On startup a `CommandLineRunner` sends one prompt to the model:

> What is the temperature in Amsterdam right now?

The `OpenApiToolBundle` reads the Open-Meteo forecast spec at runtime and exposes its
operations as tools. The model picks the right one, the library makes the HTTP call, and
the answer is printed to the console.

## Using the tools

There are two steps: **build** the tools from an OpenAPI spec, then **hand them to the
`ChatClient`**. The library does everything in between — schema generation, tool-call
dispatch, and the HTTP request to the real API.

### 1. Turn an OpenAPI spec into Spring AI tools

`OpenApiToolBundle` returns a standard Spring AI `ToolCallbackProvider`, so it plugs into
anything that accepts one:

```java
@Bean
ToolCallbackProvider weatherTools() {
    return OpenApiToolBundle
            .from("https://raw.githubusercontent.com/open-meteo/open-meteo/main/openapi/forecast.yml")
            .baseUrl("https://api.open-meteo.com")
            .build();
}
```

Every operation in `forecast.yml` becomes a tool the model can call, with parameter
schemas derived from the spec — no per-endpoint Java code. The spec has no `operationId`,
so the library derives a tool name from the method and path: `GET /v1/forecast` becomes
the tool `get_v1_forecast`.

### 2. Register the tools with the ChatClient and prompt

The provider is passed to `.defaultTools(...)`. From then on the model can call any of
the generated tools on its own when answering a prompt:

```java
@Bean
CommandLineRunner demo(ChatClient.Builder chatClientBuilder, ToolCallbackProvider weatherTools) {
    return args -> {
        var chatClient = chatClientBuilder
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .defaultTools(weatherTools)          // <-- the OpenAPI tools
                .build();

        var answer = chatClient.prompt("What is the temperature in Amsterdam right now?")
                               .call()
                               .content();

        System.out.println(answer);
    };
}
```

### What happens at runtime

1. The prompt goes to the model along with the tool definitions from the spec.
2. The model chooses the `get_v1_forecast` tool and fills in parameters — e.g.
   `latitude=52.37`, `longitude=4.89`, `current=temperature_2m`.
3. The library issues the HTTP call to
   `https://api.open-meteo.com/v1/forecast?...` and returns the JSON to the model.
4. The model reads the response and answers in natural language:

   > The temperature in Amsterdam right now is about 18 °C.

Because the library logs at `DEBUG` (see `application.yaml`), you can watch the actual
HTTP call it makes in the console:

```text
DEBUG c.s.a.o.http.OperationExecutor : [get_v1_forecast] Calling GET https://api.open-meteo.com/v1/forecast?latitude=52.37&longitude=4.89&current=temperature_2m
```

## Running it

**Requirements:** Java 25, Maven, and an Anthropic API key.

```bash
export ANTHROPIC_API_KEY=sk-ant-...
./mvnw spring-boot:run
```

## See also

- [openapi-spring-ai-tools](https://github.com/anshake/openapi-spring-ai-tools) — the library this demo uses
- [Spring AI reference documentation](https://docs.spring.io/spring-ai/reference/)
- [Open-Meteo API](https://open-meteo.com/)