package org.example;

import java.nio.file.Path;

public class RepositoryConfiguration {

    public final static Path rocksdb = Path.of("/Users/alan/swProjects/evolvedBinary/rocksdb-evolved");
    public final static Path rocksjni = rocksdb.resolve(Path.of("java/rocksjni"));
    public final static Path portal = rocksjni.resolve(Path.of("portal.h"));
}
