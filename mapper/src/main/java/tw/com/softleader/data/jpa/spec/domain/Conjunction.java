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

import java.util.Collection;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
public class Conjunction<T> extends CompoundSpecification<T> {

  public Conjunction(@NonNull Collection<Specification<T>> specs) {
    super(specs);
  }

  @Override
  protected Specification<T> combine(Specification<T> result, Specification<T> element) {
    if (element instanceof Or) {
      return result.or(element);
    }
    return result.and(element);
  }
}
