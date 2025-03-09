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

/**
 * Represents a logical conjunction (<code>AND</code>) of multiple specifications.
 *
 * <p>This annotation can be applied to a class or a field to specify that the associated {@link
 * Spec} annotations should be combined using a logical AND operation.
 *
 * <p>When applied at the class level, all fields with {@link Spec} annotations are combined with
 * <code>AND</code>. When applied at the field level, it overrides the default combination strategy
 * for that specific field.
 *
 * <p>If neither {@code @And} nor {@code @Or} is explicitly declared at the class level, <code>AND
 * </code> is the default behavior.
 *
 * @author Matt Ho
 * @see Or
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface And {}
