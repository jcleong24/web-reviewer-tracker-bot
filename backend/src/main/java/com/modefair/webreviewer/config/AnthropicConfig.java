package com.modefair.webreviewer.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes the official Anthropic Java SDK client as a Spring bean so services can
 * receive it via constructor injection.
 *
 * <p>The client resolves credentials from the environment ({@code ANTHROPIC_API_KEY});
 * no key is ever read from configuration files or hardcoded here.
 */
@Configuration
public class AnthropicConfig {

    /**
     * @return a singleton {@link AnthropicClient} configured from the environment
     */
    @Bean
    public AnthropicClient anthropicClient() {
        return AnthropicOkHttpClient.fromEnv();
    }
}
