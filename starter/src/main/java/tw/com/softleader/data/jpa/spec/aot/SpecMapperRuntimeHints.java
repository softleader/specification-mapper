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
package tw.com.softleader.data.jpa.spec.aot;

import static java.util.Arrays.stream;

import java.util.Objects;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Runtime hints for Spec Mapper AOT processing.
 *
 * @author Matt Ho
 */
@Slf4j
record SpecMapperRuntimeHints() implements RuntimeHintsRegistrar {

  private static final String REFLECT_CONFIG_LOCATION =
      "classpath*:/META-INF/spec-mapper/reflect-config.idx";

  @Override
  @SneakyThrows
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    stream(new PathMatchingResourcePatternResolver().getResources(REFLECT_CONFIG_LOCATION))
        .map(this::loadJandex)
        .flatMap(this::typeReferenceStream)
        .forEach(type -> hints.reflection().registerType(type, MemberCategory.values()));
  }

  @SneakyThrows
  private Index loadJandex(@NonNull Resource resource) {
    return new IndexReader(resource.getInputStream()).read();
  }

  private Stream<TypeReference> typeReferenceStream(@NonNull Index index) {
    return index.getKnownClasses().stream()
        .map(ClassInfo::name)
        .map(DotName::toString)
        .map(this::toTypeReferenceSafely)
        .filter(Objects::nonNull);
  }

  private TypeReference toTypeReferenceSafely(String type) {
    try {
      // 如果轉換失敗, 代表這個 type name 不是合法的, 例如 package-info 等, 就直接忽略
      return TypeReference.of(type);
    } catch (Exception e) {
      log.debug("Failed to create TypeReference for '{}', skipping", type, e);
      return null;
    }
  }
}
