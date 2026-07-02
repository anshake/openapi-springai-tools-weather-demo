# openapi-springai-tools-weather-demo

A minimal, runnable demo of [openapi-spring-ai-tools](https://github.com/anshake/openapi-spring-ai-tools).

It points the library at the [Open-Meteo](https://open-meteo.com/) OpenAPI spec, turns
every operation into a Spring AI tool, and lets an Anthropic model answer a weather
question by calling the real API — with no per-endpoint code.

## What it does

On startup a `CommandLineRunner` sends one prompt to the model:

> What is the temperature in Amsterdam right now?

The `OpenApiToolBundle` reads the Open-Meteo forecast spec at runtime and exposes its
operations as tools. The model picks the right one, the library makes the HTTP call, and
the answer is printed to the console.

```java
ToolCallbackProvider weatherTools() {
    return OpenApiToolBundle
            .from("https://raw.githubusercontent.com/open-meteo/open-meteo/main/openapi/forecast.yml")
            .baseUrl("https://api.open-meteo.com")
            .build();
}
```

## Running it

**Requirements:** Java 25, Maven, and an Anthropic API key.

```bash
export ANTHROPIC_API_KEY=sk-ant-...
./mvnw spring-boot:run
```

The prompt and the model's answer are printed at the end of the run. Library-level
activity is logged at `DEBUG` (see `application.yaml`) so you can watch the tool call
happen.

## See also

- [openapi-spring-ai-tools](https://github.com/anshake/openapi-spring-ai-tools) — the library this demo uses
- [Spring AI reference documentation](https://docs.spring.io/spring-ai/reference/)
- [Open-Meteo API](https://open-meteo.com/)