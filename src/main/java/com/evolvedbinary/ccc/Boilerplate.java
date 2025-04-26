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
                  
      """;

    private final String namespaceBegin = """
      namespace ROCKSDB_NAMESPACE {
      """;

    private final String namespaceEnd = """
      }
      """;

    public StringBuilder preamble(final StringBuilder sb) {
        sb.append(header);
        sb.append(namespaceBegin);

        return sb;
    }

    public StringBuilder postamble(final StringBuilder sb) {
        sb.append(namespaceEnd);

        return sb;
    }
}
