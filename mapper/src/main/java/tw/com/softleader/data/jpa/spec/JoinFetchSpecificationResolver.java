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

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch;
import tw.com.softleader.data.jpa.spec.annotation.JoinFetch.JoinFetches;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Context;

import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * @author Matt Ho
 */
@Slf4j
class JoinFetchSpecificationResolver implements SpecificationResolver {

  @Override
  public boolean supports(@NonNull Databind databind) {
    return databind.getTarget().getClass().isAnnotationPresent(JoinFetch.class)
        || databind.getTarget().getClass().isAnnotationPresent(JoinFetches.class);
  }

  @Override
  public Specification<Object> buildSpecification(@NonNull Context context,
      @NonNull Databind databind) {
    var handled = handledKey(databind);
    if (context.containsKey(handled)) {
      log.trace("Already handled [{}], skipping", handled);
      return null;
    }
    try {
      var specs = Stream.concat(
          joinFetchDef(databind.getTarget()),
          joinFetchesDef(databind.getTarget()))
          .filter(Objects::nonNull)
          .collect(toList());
      if (specs.size() == 1) {
        return specs.get(0);
      }
      return new Conjunction<>(specs);
    } finally {
      context.put(handled, null);
    }
  }

  private Stream<Specification<Object>> joinFetchesDef(Object obj) {
    if (!obj.getClass().isAnnotationPresent(JoinFetches.class)) {
      return Stream.empty();
    }
    return stream(obj.getClass().getAnnotation(JoinFetches.class).value())
        .map(this::newJoinFetch);
  }

  private Stream<Specification<Object>> joinFetchDef(Object obj) {
    if (!obj.getClass().isAnnotationPresent(JoinFetch.class)) {
      return Stream.empty();
    }
    return Stream.of(newJoinFetch(obj.getClass().getAnnotation(JoinFetch.class)));
  }

  Specification<Object> newJoinFetch(@NonNull JoinFetch def) {
    return new tw.com.softleader.data.jpa.spec.domain.JoinFetch<>(
        def.paths(),
        def.joinType(),
        def.distinct());
  }

  private String handledKey(Databind databind) {
    return String.join("/",
        JoinFetchSpecificationResolver.class.getName(),
        databind.getField().getDeclaringClass().getName(),
        "" + databind.getTarget().hashCode());
  }

  @Override
  public void preVisit(@lombok.NonNull SpecInvocation node) {
    // 這隻不印
  }

  @Override
  public void postVisit(@lombok.NonNull SpecInvocation node, Specification<Object> resolved) {
    // 這隻不印
  }
}
