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
package tw.com.softleader.data.jpa.spec.repository.support;

import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import tw.com.softleader.data.jpa.spec.SpecMapper;

import java.io.Serializable;

/**
 * Default implementation of {@code QueryBySpecExecutor}
 *
 * @author Matt Ho
 */
public class QueryBySpecExecutorImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
    implements QueryBySpecExecutorAdapter<T> {

  @Setter
  @Getter
  private SpecMapper specMapper;

  public QueryBySpecExecutorImpl(
      @NonNull JpaEntityInformation<T, ?> entityInformation,
      @NonNull EntityManager entityManager) {
    super(entityInformation, entityManager);
  }

  @Override
  public Class<T> getDomainClass() {
    return super.getDomainClass();
  }
}
