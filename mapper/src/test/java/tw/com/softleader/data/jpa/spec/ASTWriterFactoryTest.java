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

import static ch.qos.logback.classic.Level.DEBUG;
import static ch.qos.logback.classic.Level.INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static tw.com.softleader.data.jpa.spec.AST.CTX_AST;

import java.io.Writer;
import lombok.Builder;
import lombok.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import tw.com.softleader.data.jpa.spec.annotation.Spec;
import tw.com.softleader.data.jpa.spec.domain.Context;
import tw.com.softleader.data.jpa.spec.usecase.Gender;

/**
 * AST 只服務於 debug log, 因此 {@link ASTWriterFactory#isEnabled} 為 false 時, 整棵樹都不該被建構或輸出
 *
 * @author Matt Ho
 */
class ASTWriterFactoryTest {

  static final ch.qos.logback.classic.Logger mapperLogger = logger(SpecMapper.class);
  static final ch.qos.logback.classic.Logger criteriaLogger = logger(MyCriteria.class);

  static ch.qos.logback.classic.Logger logger(Class<?> type) {
    return (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(type);
  }

  final MyCriteria criteria = MyCriteria.builder().name("matt").gender(Gender.MALE).build();

  @AfterEach
  void tearDown() {
    mapperLogger.setLevel(null);
    criteriaLogger.setLevel(null);
  }

  @DisplayName("DEBUG 關閉時, 完全不建構 AST 也不建立 Writer")
  @Test
  void noAstWhenDebugDisabled() {
    mapperLogger.setLevel(INFO);
    var factory = spy(ASTWriterFactory.domain());
    var capturing = new AstCapturingResolver();

    var spec = mapperWith(factory, capturing).toSpec(criteria);

    assertThat(spec).isNotNull();
    verify(factory, never()).createWriter(any(), any());
    assertThat(capturing.ast).isNotNull();
    assertThat(capturing.ast.print()).isEmpty();
  }

  @DisplayName("DEBUG 開啟時, AST 照常建構並輸出")
  @Test
  void astWrittenWhenDebugEnabled() {
    mapperLogger.setLevel(DEBUG);
    var factory = spy(ASTWriterFactory.domain());
    var capturing = new AstCapturingResolver();

    var spec = mapperWith(factory, capturing).toSpec(criteria);

    assertThat(spec).isNotNull();
    verify(factory, times(1)).createWriter(any(), any());
    assertThat(capturing.ast.print()).contains(MyCriteria.class.getSimpleName());
  }

  @DisplayName("impersonation 看的是被 map 物件的 logger, 而非 SpecMapper 的")
  @Test
  void impersonationHonoursMappedObjectLogger() {
    mapperLogger.setLevel(INFO);
    criteriaLogger.setLevel(DEBUG);
    var factory = spy(ASTWriterFactory.impersonation());

    mapperWith(factory, new AstCapturingResolver()).toSpec(criteria);

    verify(factory, times(1)).createWriter(any(), any());
  }

  @DisplayName("客製 ASTWriterFactory 未覆寫 isEnabled 時, 行為不變")
  @Test
  void customFactoryKeepsReceivingAstByDefault() {
    mapperLogger.setLevel(INFO);
    criteriaLogger.setLevel(INFO);
    var factory = spy(new AlwaysOnASTWriterFactory());
    var capturing = new AstCapturingResolver();

    mapperWith(factory, capturing).toSpec(criteria);

    verify(factory, times(1)).createWriter(any(), any());
    assertThat(capturing.ast.print()).isNotEmpty();
  }

  SpecMapper mapperWith(ASTWriterFactory factory, SpecificationResolver extra) {
    return SpecMapper.builder()
        .defaultResolvers()
        .resolver(extra)
        .astWriterFactory(factory)
        .build();
  }

  /** 攔下 resolver 拿到的 AST, 用來確認樹本身有沒有被建構 */
  static class AstCapturingResolver implements SpecificationResolver {

    AST ast;

    @Override
    public boolean supports(@NonNull Databind databind) {
      return true;
    }

    @Override
    public Specification<Object> buildSpecification(Context context, Databind databind) {
      ast = context.getAs(CTX_AST, AST.class);
      return null;
    }
  }

  /** 沒有覆寫 {@link ASTWriterFactory#isEnabled} 的客製 factory */
  static class AlwaysOnASTWriterFactory implements ASTWriterFactory {

    @Override
    public Writer createWriter(@NonNull Object rootObject, Specification<Object> spec) {
      return Writer.nullWriter();
    }
  }

  @Builder
  static class MyCriteria {

    @Spec String name;

    @Spec Gender gender;
  }
}
