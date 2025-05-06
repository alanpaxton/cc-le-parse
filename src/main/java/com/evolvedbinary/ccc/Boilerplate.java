package com.evolvedbinary.ccc;

public class Boilerplate {

    private final String header = """
      // Copyright (c) 2011-present, Facebook, Inc.  All rights reserved.
      //  This source code is licensed under both the GPLv2 (found in the
      //  COPYING file in the root directory) and Apache 2.0 License
      //  (found in the LICENSE.Apache file in the root directory).
            
      // This file is designed for caching those frequently used IDs and provide
      // efficient portal (i.e, a set of static functions) to access java code
      // from c++.
            
      #pragma once
            
      #include <jni.h>
      
      #include "rocksdb/db.h"
      #include "rocksdb/status.h"
      #include "rocksjni/portal/common.h"
      """;

    private final String namespaceBegin = """
      namespace ROCKSDB_NAMESPACE {
      """;

    private final String namespaceEnd = """
      
      }  // namespace ROCKSDB_NAMESPACE
      """;

    public StringBuilder header(final StringBuilder sb) {
        sb.append(header);

        return sb;
    }

    public StringBuilder preamble(final StringBuilder sb) {
        sb.append('\n').append(namespaceBegin);

        return sb;
    }

    public StringBuilder include(final StringBuilder sb, final String namedDependency) {
        if (RepositoryConfiguration.explicitDependencies.containsKey(namedDependency)) {
            sb.append("#include \"").append(RepositoryConfiguration.explicitDependencies.get(namedDependency)).append(".h\"").append('\n');
        } else {
            sb.append("#include \"rocksjni/portal/").append(Naming.classToHeader(namedDependency)).append(".h\"").append('\n');
        }

        return sb;
    }

    public StringBuilder postamble(final StringBuilder sb) {
        sb.append(namespaceEnd);

        return sb;
    }
}
