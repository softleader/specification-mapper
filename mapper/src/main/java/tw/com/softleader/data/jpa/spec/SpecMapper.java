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

import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PACKAGE;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import tw.com.softleader.data.jpa.spec.annotation.Or;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.Disjunction;

/**
 * @author Matt Ho
 */
@Slf4j
@NoArgsConstructor(access = PACKAGE)
public class SpecMapper implements SpecCodec {

  private Collection<SpecificationResolver> resolvers; // Order matters

  public static SpecMapperBuilder builder() {
    return new SpecMapperBuilder();
  }

  @Override
  public Specification<Object> toSpec(@NonNull Context context, @Nullable Object rootObject) {
    if (rootObject == null) {
      return null;
    }
    val specs = ReflectionDatabind.of(rootObject)
        .stream()
        .flatMap(databind -> resolveSpec(context, databind))
        .filter(Objects::nonNull)
        .collect(toList());
    if (specs.isEmpty()) {
      return null;
    }
    if (rootObject.getClass().isAnnotationPresent(Or.class)) {
      return new Disjunction<>(specs);
    }
    return new Conjunction<>(specs);
  }

  Stream<Specification<Object>> resolveSpec(@NonNull Context context, @NonNull Databind databind) {
    return resolvers.stream()
        .filter(resolver -> resolver.supports(databind))
        .map(resolver -> resolver.buildSpecification(context, databind));
  }

  @NoArgsConstructor(access = PACKAGE)
  public static class SpecMapperBuilder {

    private final Collection<Function<SpecCodec, SpecificationResolver>> resolvers = new LinkedList<>();

    public SpecMapperBuilder resolver(
        @NonNull Function<SpecCodec, SpecificationResolver> resolver) {
      this.resolvers.add(resolver);
      return this;
    }

    public SpecMapperBuilder resolver(@NonNull Supplier<SpecificationResolver> resolver) {
      return resolver(codec -> resolver.get());
    }

    public SpecMapperBuilder resolver(@NonNull SpecificationResolver resolver) {
      return resolver(codec -> resolver);
    }

    public SpecMapperBuilder resolvers(@NonNull Iterable<SpecificationResolver> resolvers) {
      resolvers.forEach(this::resolver);
      return this;
    }

    public SpecMapperBuilder defaultResolvers() { // 順序是重要的, ex: Join 需要比 Simple 還早
      return resolver(NestedSpecificationResolver::new)
          .resolver(JoinFetchSpecificationResolver::new)
          .resolver(JoinSpecificationResolver::new)
          .resolver(SimpleSpecificationResolver::new);
    }

    public SpecMapper build() {
      if (this.resolvers.isEmpty()) {
        defaultResolvers();
      }
      val mapper = new SpecMapper();
      mapper.resolvers = this.resolvers.stream()
          .map(resolver -> resolver.apply(mapper))
          .collect(Collectors.collectingAndThen(
              Collectors.toList(),
              Collections::unmodifiableList));
      return mapper;
    }
  }
}
