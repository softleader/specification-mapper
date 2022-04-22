/*
 * Copyright © 2022 SoftLeader
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package tw.com.softleader.data.jpa.spec;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.nCopies;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.val;

/**
 * @author Matt Ho
 */
class SpecAST implements AST {

  private final List<String> nodes = new ArrayList<>();

  @Override
  public void add(int depth, @NonNull String message, Object... args) {
    if (depth < 0) {
      throw new IllegalArgumentException("depth must >= 0, but was " + depth);
    }
    this.nodes.add(indentLine(depth) + format(message, args));
  }

  private String indentLine(int depth) {
    val joined = join("|", nCopies(depth, "  "));
    if (joined.isEmpty()) {
      return joined;
    }
    return "|" + joined;
  }

  @Override
  public String print() {
    return join("\n", nodes);
  }
}
