package com.oopsmash.yasi;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.jface.text.BadLocationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Mojo(name = "sort", defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true, requiresDependencyResolution = ResolutionScope.NONE)
public class SortMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<String> directories = Arrays.asList(project.getBuild().getSourceDirectory(),
                project.getBuild().getTestSourceDirectory());
        for (String directory : directories) {
            try {
                scanDirectory(directory);
            } catch (IOException e) {
                throw new MojoFailureException(e.getMessage());
            }
        }
    }

    private void scanDirectory(String directory) throws IOException {
        getLog().info("Scanning " + directory);
        Path root = Paths.get(directory);
        if (!Files.exists(root)) {
            getLog().warn("Skipping " + root);
            return;
        }

        Stream<Path> paths = Files.walk(root).filter(Files::isRegularFile).filter(Files::isReadable)
                .filter(Files::isWritable).filter(path -> path.toString().endsWith(".java"));
        paths.forEach(path -> {
            try {
                sortFile(path);
            } catch (BadLocationException | IOException e) {
                getLog().error("error: " + e.getMessage());
            }
        });
        paths.close();
    }

    private void sortFile(Path path) throws BadLocationException, IOException {
        String content = readFile(path);
        String sorted = Yasi.sort(content);
        if (sorted != null) {
            writeFile(sorted, path);
        }
    }

    private String readFile(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private void writeFile(String content, Path path) throws IOException {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8),
                Files.exists(path) ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.CREATE);
    }
}