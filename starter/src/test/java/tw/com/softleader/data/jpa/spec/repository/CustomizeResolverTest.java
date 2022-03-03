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

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.val;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.Databind;
import tw.com.softleader.data.jpa.spec.SpecMapper;
import tw.com.softleader.data.jpa.spec.SpecificationResolver;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Conjunction;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.domain.Disjunction;
import tw.com.softleader.data.jpa.spec.domain.GreaterThanEqual;
import tw.com.softleader.data.jpa.spec.domain.StartingWith;
import tw.com.softleader.data.jpa.spec.repository.usecase.Customer;
import tw.com.softleader.data.jpa.spec.repository.usecase.CustomerRepository;

@Transactional
@EnableAutoConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest
class CustomizeResolverTest {

  @Autowired
  SpecMapper mapper;

  @Autowired
  CustomerRepository repository;

  @Test
  void customizeResolver() {
    val matt = repository.save(Customer.builder()
        .name("matt")
        .age(20)
        .build());
    repository.save(Customer.builder()
        .name("mary")
        .age(10)
        .build());
    repository.save(Customer.builder()
        .name("bob")
        .age(10)
        .build());

    val criteria = MyCriteria.builder()
        .age(18)
        .profile(Profile.builder()
            .name("m")
            .build())
        .build();
    val spec = mapper.toSpec(criteria);
    assertThat(spec)
        .isNotNull()
        .isInstanceOfAny(Conjunction.class, Disjunction.class)
        .hasFieldOrProperty("specs")
        .extracting("specs")
        .asInstanceOf(InstanceOfAssertFactories.LIST)
        .hasSize(2);

    val actual = repository.findBySpec(criteria);
    assertThat(actual).hasSize(1).contains(matt);

    // SQL will be:
    // select customer0_.id as id1_0_, customer0_.age as age2_0_, customer0_.name as name3_0_ from customer customer0_
    // where customer0_.age>=18
    //    and (exists (select customer1_.id from customer customer1_ where customer0_.id=customer1_.id and (customer1_.name like ?)))
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.FIELD })
  public @interface ProfileExists {

    /**
     * @return entity class
     */
    Class<?> entity();

    /**
     * @return subquery 要 join 自己的 key
     */
    String on() default "id";
  }

  @Configuration
  static class MySpecConfig {

    @Bean
    SpecificationResolver profileExistResolver(ObjectFactory<SpecMapper> mapper) {
      return new ProfileExistsSpecificationResolver(mapper::getObject);
    }
  }

  @Builder
  @Data
  public static class MyCriteria {

    @Spec(GreaterThanEqual.class)
    Integer age;

    @ProfileExists(entity = Customer.class)
    Profile profile;
  }

  @Builder
  @Data
  public static class Profile {

    @Spec(StartingWith.class)
    String name;
  }

  @AllArgsConstructor
  public static class ProfileExistsSpecificationResolver implements SpecificationResolver {

    final Supplier<SpecMapper> mapper;

    @Override
    public boolean supports(@NonNull Databind databind) {
      return databind.getField().isAnnotationPresent(ProfileExists.class);
    }

    @Override
    public Specification<Object> buildSpecification(Context context, Databind databind) {
      val def = databind.getField().getAnnotation(ProfileExists.class);
      return databind.getFieldValue()
          .map(mapper.get()::toSpec)
          .map(spec -> buildExistsSubquery(def.entity(), def.on(), spec))
          .orElse(null);
    }

    private Specification<Object> buildExistsSubquery(
        Class entityClass,
        String on,
        Specification<?> subquerySpec) {
      return (root, query, builder) -> {
        val subquery = query.subquery(entityClass);
        val subroot = subquery.from(entityClass);
        subquery
            .select(subroot)
            .where(
                builder.and(
                    builder.equal(root, subroot.get(on)),
                    subquerySpec.toPredicate(subroot, query, builder)));
        return builder.exists(subquery);
      };
    }
  }
}
