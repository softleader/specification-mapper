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

import static org.assertj.core.api.InstanceOfAssertFactories.COLLECTION;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import tw.com.softleader.data.jpa.spec.domain.Context;

@EnableAutoConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest
class SpecificationResolverCodecBuilderTest {

  @Autowired
  SpecMapper mapper;

  @Test
  void customizeCodecBuilder() {
    Assertions.assertThat(mapper)
        .extracting("resolvers", COLLECTION)
        .hasAtLeastOneElementOfType(MySpecificationResolver.class);
  }

  @Configuration
  static class MySpecConfig {

    @Bean
    SpecificationResolverCodecBuilder mySpecificationResolver() {
      return MySpecificationResolver::new;
    }
  }

  @AllArgsConstructor
  public static class MySpecificationResolver implements SpecificationResolver {

    final SpecCodec codec;

    @Override
    public boolean supports(@NonNull Databind databind) {
      return true;
    }

    @Override
    public Specification<Object> buildSpecification(Context context, Databind databind) {
      return null;
    }
  }
}
