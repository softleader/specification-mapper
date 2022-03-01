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
package tw.com.softleader.data.jpa.spec.domain.yet_another_package;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static tw.com.softleader.data.jpa.spec.IntegrationTest.TestApplication.noopContext;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.SimpleSpecification;

class ConstructSimpleSpecificationTest {

  @Test
  void newProtectedConstructorSpec() {
    assertThatNoException()
        .isThrownBy(() -> SimpleSpecification.builder()
            .context(noopContext())
            .domainClass(ProtectedConstructorSpec.class)
            .path("")
            .value(new Object())
            .build());
  }

  @Test
  void newDefaultConstructorSpec() {
    assertThatNoException()
        .isThrownBy(() -> SimpleSpecification.builder()
            .context(noopContext())
            .domainClass(DefaultConstructorSpec.class)
            .path("")
            .value(new Object())
            .build());
  }

  @Test
  void newPrivateConstructorSpec() {
    assertThatNoException()
        .isThrownBy(() -> SimpleSpecification.builder()
            .context(noopContext())
            .domainClass(PrivateConstructorSpec.class)
            .path("")
            .value(new Object())
            .build());
  }

  @Test
  void newDefaultClassSpec() {
    assertThatNoException()
        .isThrownBy(() -> SimpleSpecification.builder()
            .context(noopContext())
            .domainClass(DefaultClassSpec.class)
            .path("")
            .value(new Object())
            .build());
  }

  public static class ProtectedConstructorSpec extends SimpleSpecification<Object> {

    protected ProtectedConstructorSpec(
        @NonNull Context context, @NonNull String path,
        @NonNull Object value) {
      super(context, path, value);
    }

    @Override
    public Predicate toPredicate(Root root,
        CriteriaQuery query,
        CriteriaBuilder criteriaBuilder) {
      return null;
    }
  }

  public static class DefaultConstructorSpec extends SimpleSpecification<Object> {

    DefaultConstructorSpec(
        @NonNull Context context, @NonNull String path,
        @NonNull Object value) {
      super(context, path, value);
    }

    @Override
    public Predicate toPredicate(Root root,
        CriteriaQuery query,
        CriteriaBuilder criteriaBuilder) {
      return null;
    }
  }

  public static class PrivateConstructorSpec extends SimpleSpecification<Object> {

    private PrivateConstructorSpec(
        @NonNull Context context, @NonNull String path,
        @NonNull Object value) {
      super(context, path, value);
    }

    @Override
    public Predicate toPredicate(Root root,
        CriteriaQuery query,
        CriteriaBuilder criteriaBuilder) {
      return null;
    }
  }
}

class DefaultClassSpec extends SimpleSpecification<Object> {

  private DefaultClassSpec(
      @NonNull Context context, @NonNull String path,
      @NonNull Object value) {
    super(context, path, value);
  }

  @Override
  public Predicate toPredicate(Root root,
      CriteriaQuery query,
      CriteriaBuilder criteriaBuilder) {
    return null;
  }
}
