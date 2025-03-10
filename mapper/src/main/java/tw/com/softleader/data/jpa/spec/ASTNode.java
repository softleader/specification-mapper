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

import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

/**
 * Represents a node within an AST that supports pre- and post-visitation hooks. This interface
 * provides default implementations for logging AST structures during the resolution of
 * specifications.
 *
 * @author Matt Ho
 */
public interface ASTNode {

  /**
   * Called before processing a specification invocation. Logs the invocation details to the AST.
   *
   * @param node the specification invocation being processed, must not be null
   */
  default void preVisit(@NonNull SpecInvocation node) {
    node.getAst()
        .add(
            node.getDepth(),
            "|  +-[%s.%s]: %s (%s)",
            node.getTargetType().getSimpleName(),
            node.getFieldName(),
            node.getFieldType().getName(),
            node.getResolverType().getSimpleName());
  }

  /**
   * Called after processing a specification invocation. Logs the resolved specification result to
   * the AST.
   *
   * @param node the specification invocation that was processed, must not be null
   * @param resolved the resolved specification, may be null
   */
  default void postVisit(@NonNull SpecInvocation node, @Nullable Specification<Object> resolved) {
    node.getAst()
        .add(
            node.getDepth(),
            "|  \\-[%s.%s]: %s",
            node.getTargetType().getSimpleName(),
            node.getFieldName(),
            resolved);
  }
}
