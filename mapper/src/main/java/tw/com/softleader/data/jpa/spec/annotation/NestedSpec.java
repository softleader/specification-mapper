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
import tw.com.softleader.data.jpa.spec.SpecMapper;

/**
 * Marks a field as a nested object for {@link SpecMapper} to recursively combine specifications.
 * Supports deep nesting with no level limits. Defaults to {@link And} logic unless explicitly
 * annotated with {@link Or}.
 *
 * <p>Example:
 *
 * <pre>{@code
 * public class CustomerCriteria {
 *   @Spec(Like.class) String firstname;
 *   @NestedSpec AddressCriteria address;
 * }
 *
 * @Or
 * public class AddressCriteria {
 *   @Spec String county;
 *   @Spec String city;
 * }
 * }</pre>
 *
 * @author Matt Ho
 * @see Spec
 * @see And
 * @see Or
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface NestedSpec {}
