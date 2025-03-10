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

/**
 * Represents an invocation of a specification resolver within the AST. This interface provides
 * access to contextual information about the specification resolution process, including depth,
 * target type, field details, and the associated resolver.
 *
 * @author Matt Ho
 */
public interface SpecInvocation {

  /**
   * Returns the AST instance associated with this invocation.
   *
   * @return the AST instance
   */
  AST getAst();

  /**
   * Returns the depth level of this invocation in the AST.
   *
   * @return the depth level
   */
  int getDepth();

  /**
   * Returns the type of the specification resolver handling this invocation.
   *
   * @return the class of the specification resolver
   */
  Class<? extends SpecificationResolver> getResolverType();

  /**
   * Returns the type of the target object being evaluated in this invocation.
   *
   * @return the class of the target type
   */
  Class<?> getTargetType();

  /**
   * Returns the type of the field being evaluated in this invocation.
   *
   * @return the class of the field type
   */
  Class<?> getFieldType();

  /**
   * Returns the name of the field being evaluated in this invocation.
   *
   * @return the field name
   */
  String getFieldName();
}
