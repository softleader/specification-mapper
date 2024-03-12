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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.reflection;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import tw.com.softleader.data.jpa.spec.aot.SpecMapperRuntimeHints;
import tw.com.softleader.data.jpa.spec.domain.*;

class SpecRuntimeHintsTest {

  @Test
  void shouldRegisterHints() {
    var hints = new RuntimeHints();
    new SpecMapperRuntimeHints().registerHints(hints, getClass().getClassLoader());
    assertThat(reflection().onType(AST.class)).accepts(hints);
    assertThat(reflection().onType(ASTNode.class)).accepts(hints);
    assertThat(reflection().onType(Databind.class)).accepts(hints);
    assertThat(reflection().onType(DefaultSkippingStrategy.class)).accepts(hints);
    assertThat(reflection().onType(JoinFetchSpecificationResolver.class)).accepts(hints);
    assertThat(reflection().onType(JoinSpecificationResolver.class)).accepts(hints);
    assertThat(reflection().onType(NestedSpecificationResolver.class)).accepts(hints);
    assertThat(reflection().onType(ReflectionDatabind.class)).accepts(hints);
    assertThat(reflection().onType(ReflectionDatabindFactory.class)).accepts(hints);
    assertThat(reflection().onType(ReflectionSpecInvocation.class)).accepts(hints);
    assertThat(reflection().onType(SimpleSpecificationResolver.class)).accepts(hints);
    assertThat(reflection().onType(SkippingStrategy.class)).accepts(hints);
    assertThat(reflection().onType(SpecAST.class)).accepts(hints);
    assertThat(reflection().onType(SpecCodec.class)).accepts(hints);
    assertThat(reflection().onType(SpecContext.class)).accepts(hints);
    assertThat(reflection().onType(SpecificationResolver.class)).accepts(hints);
    assertThat(reflection().onType(SpecInvocation.class)).accepts(hints);
    assertThat(reflection().onType(SpecJoinContext.class)).accepts(hints);
    assertThat(reflection().onType(SpecMapper.class)).accepts(hints);
  }
}
