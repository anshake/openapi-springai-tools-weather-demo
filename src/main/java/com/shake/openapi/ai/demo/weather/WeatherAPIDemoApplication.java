package com.shake.openapi.ai.demo.weather;

import com.shake.ai.openapi.OpenApiToolBundle;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WeatherAPIDemoApplication
{

    private static final String PROMPT = "What is the temperature in Amsterdam right now?";

    @Bean
    ToolCallbackProvider weatherTools() {
        return OpenApiToolBundle
                .from("https://raw.githubusercontent.com/open-meteo/open-meteo/main/openapi/forecast.yml")
                .baseUrl("https://api.open-meteo.com")
                .build();
    }


    @Bean
    CommandLineRunner demo(ChatClient.Builder chatClientBuilder, ToolCallbackProvider weatherTools) {
        return args -> {
            var chatClient = chatClientBuilder
                    .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                    .defaultTools(weatherTools)
                    .build();

            var answer = chatClient.prompt(PROMPT)
                                   .call()
                                   .content();

            System.out.println("\n=== Prompt ===\n" + PROMPT);
            System.out.println("\n=== LLM ===\n" + answer + "\n");
        };
    }

    public static void main(String[] args)
    {
        SpringApplication.run(WeatherAPIDemoApplication.class, args);
    }

}
