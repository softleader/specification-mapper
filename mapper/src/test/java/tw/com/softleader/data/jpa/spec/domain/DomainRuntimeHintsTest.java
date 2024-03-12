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
package tw.com.softleader.data.jpa.spec.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.reflection;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import tw.com.softleader.data.jpa.spec.*;
import tw.com.softleader.data.jpa.spec.aot.SpecMapperRuntimeHints;

class DomainRuntimeHintsTest {

  @Test
  void shouldRegisterHints() {
    var hints = new RuntimeHints();
    new SpecMapperRuntimeHints().registerHints(hints, getClass().getClassLoader());
    assertThat(reflection().onType(After.class)).accepts(hints);
    assertThat(reflection().onType(And.class)).accepts(hints);
    assertThat(reflection().onType(Before.class)).accepts(hints);
    assertThat(reflection().onType(Between.class)).accepts(hints);
    assertThat(reflection().onType(BooleanSpecification.class)).accepts(hints);
    assertThat(reflection().onType(ComparableSpecification.class)).accepts(hints);
    assertThat(reflection().onType(CompoundSpecification.class)).accepts(hints);
    assertThat(reflection().onType(Conjunction.class)).accepts(hints);
    assertThat(reflection().onType(Context.class)).accepts(hints);
    assertThat(reflection().onType(Disjunction.class)).accepts(hints);
    assertThat(reflection().onType(EndingWith.class)).accepts(hints);
    assertThat(reflection().onType(Equals.class)).accepts(hints);
    assertThat(reflection().onType(False.class)).accepts(hints);
    assertThat(reflection().onType(GreaterThan.class)).accepts(hints);
    assertThat(reflection().onType(GreaterThanEqual.class)).accepts(hints);
    assertThat(reflection().onType(HasLength.class)).accepts(hints);
    assertThat(reflection().onType(HasText.class)).accepts(hints);
    assertThat(reflection().onType(In.class)).accepts(hints);
    assertThat(reflection().onType(IsNull.class)).accepts(hints);
    assertThat(reflection().onType(Join.class)).accepts(hints);
    assertThat(reflection().onType(JoinContext.class)).accepts(hints);
    assertThat(reflection().onType(JoinFetch.class)).accepts(hints);
    assertThat(reflection().onType(LessThan.class)).accepts(hints);
    assertThat(reflection().onType(LessThanEqual.class)).accepts(hints);
    assertThat(reflection().onType(Like.class)).accepts(hints);
    assertThat(reflection().onType(Not.class)).accepts(hints);
    assertThat(reflection().onType(NotEquals.class)).accepts(hints);
    assertThat(reflection().onType(NotIn.class)).accepts(hints);
    assertThat(reflection().onType(NotLike.class)).accepts(hints);
    assertThat(reflection().onType(NotNull.class)).accepts(hints);
    assertThat(reflection().onType(Or.class)).accepts(hints);
    assertThat(reflection().onType(SimpleSpecification.class)).accepts(hints);
    assertThat(reflection().onType(StartingWith.class)).accepts(hints);
    assertThat(reflection().onType(True.class)).accepts(hints);
    assertThat(reflection().onType(TypeMismatchException.class)).accepts(hints);
  }
}
