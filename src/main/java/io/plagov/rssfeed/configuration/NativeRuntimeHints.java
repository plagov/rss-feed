package io.plagov.rssfeed.configuration;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(NativeRuntimeHints.FlywayHints.class)
public class NativeRuntimeHints {

    static class FlywayHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection().registerType(
                TypeReference.of("org.flywaydb.core.internal.configuration.extensions.PrepareScriptFilenameConfigurationExtension"),
                MemberCategory.INVOKE_PUBLIC_METHODS
            );
        }
    }
}
