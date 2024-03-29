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
package tw.com.softleader.data.jpa.spec.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import tw.com.softleader.data.jpa.spec.repository.support.QueryBySpecExecutorAdapter;
import tw.com.softleader.data.jpa.spec.repository.support.QueryBySpecExecutorImpl;

/**
 * @author Matt Ho
 */
@Data
@ConfigurationProperties(prefix = SpecMapperProperties.PREFIX_SPEC_MAPPER)
public class SpecMapperProperties {

  public static final String PREFIX_SPEC_MAPPER = "spec.mapper";

  /** Whether to enable the spec mapper */
  boolean enabled = true;

  /**
   * Configures the repository base class. the given class must implement QueryBySpecExecutorAdapter
   *
   * @see QueryBySpecExecutorAdapter
   * @see QueryBySpecExecutorImpl
   */
  Class<? extends QueryBySpecExecutorAdapter> repositoryBaseClass = QueryBySpecExecutorImpl.class;

  /**
   * Whether to impersonate the logger, if enabled, the actual logger of the changed object will be
   * used, otherwise the SpecMapper logger will be used
   */
  boolean impersonateLogger;
}
