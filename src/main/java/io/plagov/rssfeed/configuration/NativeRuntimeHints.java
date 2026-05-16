package io.plagov.rssfeed.configuration;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints({NativeRuntimeHints.FlywayHints.class, NativeRuntimeHints.AppHints.class})
public class NativeRuntimeHints {

    static class AppHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.resources().registerPattern("prompts/*.st");

            hints.reflection().registerType(
                io.plagov.rssfeed.domain.response.AiPostEvaluation.class,
                MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS
            );

            hints.reflection().registerType(
                JwtProperties.class,
                MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS
            );

            hints.reflection().registerType(
                CorsProperties.class,
                MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS
            );
        }
    }

    static class FlywayHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection().registerType(
                TypeReference.of("org.flywaydb.core.internal.configuration.extensions.PrepareScriptFilenameConfigurationExtension"),
                MemberCategory.INVOKE_PUBLIC_METHODS
            );
            hints.reflection().registerType(
                TypeReference.of("org.flywaydb.database.postgresql.PostgreSQLConfigurationExtension"),
                MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS
            );
            hints.reflection().registerType(
                TypeReference.of("org.flywaydb.database.postgresql.TransactionalModel"),
                MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS
            );
        }
    }
}
