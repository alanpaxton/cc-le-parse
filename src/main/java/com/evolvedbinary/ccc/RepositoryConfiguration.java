package com.evolvedbinary.ccc;

import java.nio.file.Path;
import java.util.Map;

public class RepositoryConfiguration {

    public final static Path rocksdb = Path.of("/Users/alan/swProjects/evolvedBinary/rocksdb-eb2");
    public final static Path rocksjni = rocksdb.resolve(Path.of("java/rocksjni"));
    public final static Path portal = rocksjni.resolve(Path.of("portal.h"));

    public final static Map<String, Path> explicitDependencies = Map.of(
      "WriteBatchHandlerJniCallback", rocksjni.resolve("write_batch_handler_jni.h"));


}
