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
package tw.com.softleader.data.jpa.spec.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Interface to allow execution of Query by Spec instances.
 *
 * @author Matt Ho
 */
@NoRepositoryBean
public interface QueryBySpecExecutor<T> {

  List<T> findBySpec(@Nullable Object spec);

  List<T> findBySpec(@Nullable Object spec, @NonNull Sort sort);

  Page<T> findBySpec(@Nullable Object spec, @NonNull Pageable pageable);

  long countBySpec(@Nullable Object spec);

  boolean existsBySpec(@Nullable Object spec);
}
