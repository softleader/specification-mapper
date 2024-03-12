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
import static tw.com.softleader.data.jpa.spec.AST.CTX_AST;
import static tw.com.softleader.data.jpa.spec.AST.CTX_DEPTH;
import static tw.com.softleader.data.jpa.spec.WriterStrategy.domainWriterStrategy;

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
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import tw.com.softleader.data.jpa.spec.annotation.Or;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.Disjunction;

/**
 * @author Matt Ho
 */
@RequiredArgsConstructor(access = PACKAGE)
public class SpecMapper implements SpecCodec {

  @NonNull private final SkippingStrategy skippingStrategy;
  @NonNull private final WriterStrategy writerStrategy;
  private Collection<SpecificationResolver> resolvers; // Order matters

  public static SpecMapperBuilder builder() {
    return new SpecMapperBuilder();
  }

  @Override
  @SneakyThrows
  public Specification<Object> toSpec(Object rootObject) {
    if (rootObject == null) {
      return null;
    }
    var context = new SpecContext();
    var ast = new SpecAST();
    var depth = 0;
    context.put(CTX_AST, ast);
    context.put(CTX_DEPTH, depth);
    ast.add(
        depth,
        "+-[%s]: %s",
        rootObject.getClass().getSimpleName(),
        rootObject.getClass().getName());
    var spec = toSpec(context, rootObject);
    ast.add(depth, "\\-[%s]: %s", rootObject.getClass().getSimpleName(), spec);
    ast.write(writerStrategy.getWriter(rootObject, spec));
    return spec;
  }

  @Override
  public Specification<Object> toSpec(@NonNull Context context, @Nullable Object rootObject) {
    if (rootObject == null) {
      return null;
    }
    var specs =
        ReflectionDatabind.of(rootObject, skippingStrategy).stream()
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
        .map(resolver -> resolveSpec(context, databind, resolver));
  }

  Specification<Object> resolveSpec(
      @NonNull Context context,
      @NonNull Databind databind,
      @NonNull SpecificationResolver resolver) {
    var node =
        new ReflectionSpecInvocation(
            context.get(CTX_AST).map(AST.class::cast).get(),
            (int) context.get(CTX_DEPTH).get(),
            resolver,
            databind);
    resolver.preVisit(node);
    var resolved = resolver.buildSpecification(context, databind);
    resolver.postVisit(node, resolved);
    return resolved;
  }

  @NoArgsConstructor(access = PACKAGE)
  public static class SpecMapperBuilder {

    private final Collection<Function<SpecCodec, SpecificationResolver>> resolvers =
        new LinkedList<>();
    private SkippingStrategy skippingStrategy = new DefaultSkippingStrategy();
    private WriterStrategy writerStrategy = domainWriterStrategy();

    public SpecMapperBuilder writerStrategy(@NonNull WriterStrategy strategy) {
      this.writerStrategy = strategy;
      return this;
    }

    public SpecMapperBuilder skippingStrategy(@NonNull SkippingStrategy strategy) {
      this.skippingStrategy = strategy;
      return this;
    }

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
      var mapper = new SpecMapper(skippingStrategy, writerStrategy);
      mapper.resolvers =
          this.resolvers.stream()
              .map(resolver -> resolver.apply(mapper))
              .collect(
                  Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
      return mapper;
    }
  }
}
