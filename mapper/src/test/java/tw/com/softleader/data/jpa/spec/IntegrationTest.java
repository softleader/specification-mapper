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

import java.lang.annotation.*;
import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.JoinContext;

/**
 * Integration test with Spring Boot Data JPA
 *
 * @author Matt Ho
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@DataJpaTest
public @interface IntegrationTest {

  /**
   * Main entrypoint for running test Spring Boot
   *
   * @author Matt Ho
   */
  @SpringBootApplication
  class TestApplication {

    public static Context noopContext() {
      return new Context() {
        @Override
        public JoinContext join() {
          throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
          throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
          throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
          throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key) {
          throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsValue(Object value) {
          throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Object> get(Object key) {
          throw new UnsupportedOperationException();
        }

        @Override
        public Object put(Object key, Object value) {
          throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(Object key) {
          throw new UnsupportedOperationException();
        }
      };
    }

    public static void main(String[] args) {
      SpringApplication.run(TestApplication.class, args);
    }
  }
}
