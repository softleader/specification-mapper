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

import java.io.IOException;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.SneakyThrows;
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
record SpecMapperRuntimeHints() implements RuntimeHintsRegistrar {

  private static final String REFLECT_CONFIG = "META-INF/spec-mapper/reflect-config.idx";
  private static final String REFLECT_CONFIG_LOCATION = "classpath*:/" + REFLECT_CONFIG;

  @Override
  @SneakyThrows
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    registerMetaInfHints(hints);
    registerSourceHints(hints);
  }

  private void registerMetaInfHints(RuntimeHints hints) {
    hints.resources().registerPattern(REFLECT_CONFIG);
  }

  private void registerSourceHints(RuntimeHints hints) throws IOException {
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
        .map(TypeReference::of);
  }
}
