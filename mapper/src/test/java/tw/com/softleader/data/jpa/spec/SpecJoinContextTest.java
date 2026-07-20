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

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static tw.com.softleader.data.jpa.spec.SpecJoinContext.HandleKey.identityHex;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Root;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tw.com.softleader.data.jpa.spec.SpecJoinContext.HandleKey;
import tw.com.softleader.data.jpa.spec.domain.JoinContext.FetchRef;
import tw.com.softleader.data.jpa.spec.usecase.Customer;

@IntegrationTest
class SpecJoinContextTest {

  @Autowired EntityManager entityManager;

  @Test
  void shouldConvertTargetAndFieldToIdentityHex() throws NoSuchFieldException {
    var target = new Object();
    var annotation = mock(Annotation.class);
    var field = TargetA.class.getDeclaredField("field");

    var key = new HandleKey(target, field, annotation);

    var expectedField = identityHex(field);
    assertThat(key.target()).isEqualTo(identityHex(target));
    assertThat(key.field()).isEqualTo(expectedField);
    assertThat(key.def()).isEqualTo(annotation);
  }

  @Test
  void shouldHandleNullField() {
    var target = new Object();
    var annotation = mock(Annotation.class);

    var key = new HandleKey(target, null, annotation);

    assertThat(key.target()).isEqualTo(identityHex(target));
    assertThat(key.field()).isNull();
    assertThat(key.def()).isEqualTo(annotation);
  }

  @Test
  void shouldGenerateDifferentKeysForNullFieldInDifferentClasses() {
    var annotation = mock(Annotation.class);

    var keyA = new HandleKey(new Object(), null, annotation);
    var keyB = new HandleKey(new Object(), null, annotation);

    assertThat(keyA).isNotEqualTo(keyB);
    assertThat(keyA.field()).isNull();
    assertThat(keyA.def()).isEqualTo(annotation);
    assertThat(keyB.field()).isNull();
    assertThat(keyB.def()).isEqualTo(annotation);
  }

  @Test
  void shouldGenerateDifferentKeysForSameFieldNameInDifferentClasses() throws NoSuchFieldException {
    var target = new Object();
    var annotation = mock(Annotation.class);

    var fieldA = TargetA.class.getDeclaredField("field");
    var fieldB = TargetB.class.getDeclaredField("field");

    var keyA = new HandleKey(target, fieldA, annotation);
    var keyB = new HandleKey(target, fieldB, annotation);

    assertThat(keyA).isNotEqualTo(keyB);
    assertThat(keyA.field()).isNotEqualTo(keyB.field());
  }

  @Test
  void shouldGenerateDifferentKeysForSameTypeFieldsInSameClass() throws NoSuchFieldException {
    var target = new Object();
    var annotation = mock(Annotation.class);

    var field1 = TargetC.class.getDeclaredField("fieldA");
    var field2 = TargetC.class.getDeclaredField("fieldB");

    var key1 = new HandleKey(target, field1, annotation);
    var key2 = new HandleKey(target, field2, annotation);

    assertThat(key1).isNotEqualTo(key2);
    assertThat(key1.field()).isNotEqualTo(key2.field());
  }

  @DisplayName("join/fetch 的登錄資料應隨著 Root 一起被回收, 不應無限累積")
  @Test
  void shouldNotRetainBookkeepingOfCollectedRoot() {
    var context = new SpecJoinContext();
    var cb = entityManager.getCriteriaBuilder();

    var query = cb.createQuery(Customer.class);
    Root<Customer> root = query.from(Customer.class);
    context.putIfAbsent(root, "o", root.join("orders"));
    context.putIfAbsent(root, "b", new FetchRef(root.fetch("badges"), "badges"));

    assertThat(context.getJoin(root, "o")).isNotNull();
    assertThat(context.getFetch(root, "b")).isNotNull();

    var collected = new WeakReference<>(root);
    // 放掉這次執行所建立的 criteria tree
    root = null;
    query = null;

    await()
        .atMost(ofSeconds(10))
        .untilAsserted(
            () -> {
              System.gc();
              assertThat(collected.get()).isNull();
              assertThat(context).extracting("joined", MAP).isEmpty();
              assertThat(context).extracting("fetched", MAP).isEmpty();
            });
  }

  static class TargetA {
    private String field;
  }

  static class TargetB {
    private String field;
  }

  static class TargetC {
    private String fieldA;
    private String fieldB;
  }
}
