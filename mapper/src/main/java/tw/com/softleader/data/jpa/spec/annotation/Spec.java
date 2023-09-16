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
package tw.com.softleader.data.jpa.spec.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Path;
import tw.com.softleader.data.jpa.spec.domain.Equals;
import tw.com.softleader.data.jpa.spec.domain.SimpleSpecification;

/**
 * @author Matt Ho
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Spec {

  /**
   * Name of the attribute to create {@code Path}
   *
   * @see Path
   */
  String path() default "";

  /**
   * {@code Specification} domain class
   */
  Class<? extends SimpleSpecification> value() default Equals.class;

  /**
   * Negates the {@code Specification}.
   *
   * @see Specification#not(Specification)
   */
  boolean not() default false;
}
