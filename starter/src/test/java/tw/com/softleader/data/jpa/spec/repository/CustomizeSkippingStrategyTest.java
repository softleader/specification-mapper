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
package tw.com.softleader.data.jpa.spec.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.transaction.annotation.Transactional;

import tw.com.softleader.data.jpa.spec.SkippingStrategy;
import tw.com.softleader.data.jpa.spec.SpecMapper;

@Transactional
@EnableAutoConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest
class CustomizeSkippingStrategyTest {

  @Autowired
  SpecMapper mapper;

  @Test
  void customizeSkippingStrategy() {
    Assertions.assertThat(mapper)
        .extracting("skippingStrategy")
        .isInstanceOf(MySkippingStrategy.class);
  }

  @Configuration
  static class MySpecConfig {

    @Bean
    SkippingStrategy skippingStrategy() {
      return new MySkippingStrategy();
    }
  }

  static class MySkippingStrategy implements SkippingStrategy {

    @Override
    public boolean shouldSkip(Object fieldValue) {
      return false;
    }
  }
}
