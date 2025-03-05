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
import static tw.com.softleader.data.jpa.spec.ASTWriterFactory.domain;
import static tw.com.softleader.data.jpa.spec.domain.JoinContext.CTX_JOIN;

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
 * Implementation of {@link SpecCodec} that maps objects to {@link Specification} instances using a
 * collection of {@link SpecificationResolver}.
 *
 * <p>This class processes the given object by traversing its properties, resolving applicable
 * specifications, and combining them into a logical conjunction or disjunction based on annotations
 * present on the object.
 *
 * <p>Order of resolvers matters; for example, join resolvers must precede simple resolvers.
 *
 * @author Matt Ho
 */
@RequiredArgsConstructor(access = PACKAGE)
public class SpecMapper implements SpecCodec {

  @NonNull private final SkippingStrategy skippingStrategy;
  @NonNull private final ASTWriterFactory astWriterFactory;
  private Collection<SpecificationResolver> resolvers; // Order matters

  /**
   * Creates a new builder for constructing a {@link SpecMapper} instance.
   *
   * @return a new {@link SpecMapperBuilder}
   */
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
    context.put(CTX_JOIN, new SpecJoinContext());
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
    try (var writer = astWriterFactory.createWriter(rootObject, spec)) {
      ast.write(writer);
    }
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
            context.getAs(CTX_AST, AST.class),
            context.getAs(CTX_DEPTH, Integer.class),
            resolver,
            databind);
    resolver.preVisit(node);
    var resolved = resolver.buildSpecification(context, databind);
    resolver.postVisit(node, resolved);
    return resolved;
  }

  /**
   * Builder for {@link SpecMapper} instances.
   *
   * @author Matt Ho
   */
  @NoArgsConstructor(access = PACKAGE)
  public static class SpecMapperBuilder {

    private final Collection<Function<SpecCodec, SpecificationResolver>> resolvers =
        new LinkedList<>();
    private SkippingStrategy skippingStrategy = new DefaultSkippingStrategy();
    private ASTWriterFactory astWriterFactory = domain();

    /**
     * Sets the AST writer factory.
     *
     * @param factory the {@link ASTWriterFactory} to use
     * @return this builder instance
     */
    public SpecMapperBuilder astWriterFactory(@NonNull ASTWriterFactory factory) {
      this.astWriterFactory = factory;
      return this;
    }

    /**
     * Sets the skipping strategy.
     *
     * @param strategy the {@link SkippingStrategy} to use
     * @return this builder instance
     */
    public SpecMapperBuilder skippingStrategy(@NonNull SkippingStrategy strategy) {
      this.skippingStrategy = strategy;
      return this;
    }

    /**
     * Adds a resolver function.
     *
     * @param resolver the resolver function
     * @return this builder instance
     */
    public SpecMapperBuilder resolver(
        @NonNull Function<SpecCodec, SpecificationResolver> resolver) {
      this.resolvers.add(resolver);
      return this;
    }

    /**
     * Adds a resolver supplier.
     *
     * @param resolver the resolver supplier
     * @return this builder instance
     */
    public SpecMapperBuilder resolver(@NonNull Supplier<SpecificationResolver> resolver) {
      return resolver(codec -> resolver.get());
    }

    /**
     * Adds a specific resolver instance.
     *
     * @param resolver the resolver instance
     * @return this builder instance
     */
    public SpecMapperBuilder resolver(@NonNull SpecificationResolver resolver) {
      return resolver(codec -> resolver);
    }

    /**
     * Adds multiple resolvers.
     *
     * @param resolvers the resolvers to add
     * @return this builder instance
     */
    public SpecMapperBuilder resolvers(@NonNull Iterable<SpecificationResolver> resolvers) {
      resolvers.forEach(this::resolver);
      return this;
    }

    /**
     * Adds default resolvers in the appropriate order.
     *
     * @return this builder instance
     */
    public SpecMapperBuilder defaultResolvers() { // 順序是重要的, ex: Join 需要比 Simple 還早
      return resolver(NestedSpecificationResolver::new)
          .resolver(JoinFetchSpecificationResolver::new)
          .resolver(JoinSpecificationResolver::new)
          .resolver(SimpleSpecificationResolver::new);
    }

    /**
     * Builds and returns a {@link SpecMapper} instance.
     *
     * @return the constructed {@link SpecMapper}
     */
    public SpecMapper build() {
      if (this.resolvers.isEmpty()) {
        defaultResolvers();
      }
      var mapper = new SpecMapper(skippingStrategy, astWriterFactory);
      mapper.resolvers =
          this.resolvers.stream()
              .map(resolver -> resolver.apply(mapper))
              .collect(
                  Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
      return mapper;
    }
  }
}
