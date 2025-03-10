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
import tw.com.softleader.data.jpa.spec.domain.Context;

/**
 * Represents an Abstract Syntax Tree (AST) used for logging and debugging purposes. This interface
 * provides methods to add hierarchical log entries and print the AST.
 *
 * @author Matt Ho
 */
public interface AST {

  /**
   * The context key for storing {@code AST} instance.
   *
   * @see Context
   */
  String CTX_AST = "_AST";

  /**
   * The context key for storing current depth level.
   *
   * @see Context
   */
  String CTX_DEPTH = "_DEPTH";

  /**
   * Adds a formatted message to the AST at the specified depth.
   *
   * @param depth the depth level in the AST hierarchy
   * @param message the format string of the log message, must not be null
   * @param args the arguments referenced by the format specifiers in the message
   */
  void add(int depth, @NonNull String message, Object... args);

  /**
   * Prints the current AST structure as a string.
   *
   * @return the formatted AST representation
   */
  String print();
}
