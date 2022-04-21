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

import static java.lang.String.join;
import static java.util.Collections.nCopies;

import lombok.Value;
import lombok.val;

@Value
class Depth {

  static final String DEPTH = Depth.class.getName();

  int level;
  String tree;

  Depth(int level) {
    if (level < 0) {
      throw new IllegalArgumentException("depth level must >= 0, but was " + level);
    }
    this.level = level;
    this.tree = drawTree();
  }

  private String drawTree() {
    val joined = join("|", nCopies(level, "  "));
    if (joined.isEmpty()) {
      return joined;
    }
    return "|" + joined;
  }
}
