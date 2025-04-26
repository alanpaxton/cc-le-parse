package com.evolvedbinary.ccc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileOutput {

    private final CharSequence contents;
    private final File file;

    public File getFile() {
        return file;
    }

    public FileOutput(final Path filePath, final CharSequence contents) throws IOException {
        this.contents = contents;
        Path resolvedFilePath = RepositoryConfiguration.rocksjni.resolve(filePath);
        this.file = resolvedFilePath.toFile();
        Files.createDirectories(resolvedFilePath.getParent());
    }

    public final void write() throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(contents.toString());
        fileWriter.close();
    }
}
