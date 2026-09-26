package kr.hwaryuh.purity;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

// Java because Kotlin stdlib is not on the classpath yet.
@SuppressWarnings("UnstableApiUsage")
public class PurityLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder(
            "central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR
        ).build());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("paper-library")),
            StandardCharsets.UTF_8
        ))) {
            reader.lines().map(String::trim).filter(s -> !s.isEmpty())
                .forEach(s -> resolver.addDependency(new Dependency(new DefaultArtifact(s), null)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        classpathBuilder.addLibrary(resolver);
    }
}
