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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.Writer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

/**
 * This is an interface for defining strategies to get writer for AST.
 *
 * @author Matt Ho
 */
public interface WriterStrategy {

  /**
   * This method is used to get writer for AST.
   *
   * @param rootObject The target object to be mapped, never null
   * @param spec The specification to be mapped, cloud be null
   */
  Writer getWriter(@NonNull Object rootObject, @Nullable Specification<Object> spec);

  static WriterStrategy domainWriterStrategy() {
    return (rootObject, spec) -> new Slf4jDebugWriter(getLogger(SpecMapper.class));
  }

  static WriterStrategy impersonateWriterStrategy() {
    return (rootObject, spec) -> new Slf4jDebugWriter(getLogger(rootObject.getClass()));
  }
}

@RequiredArgsConstructor
class Slf4jDebugWriter extends Writer {

  @NonNull private final Logger logger;
  private final StringBuilder buffer = new StringBuilder();

  public void write(char ch) {
    if (ch == '\n' && !buffer.isEmpty()) {
      logger.debug(this.buffer.toString());
      buffer.setLength(0);
      return;
    }
    buffer.append(ch);
  }

  @Override
  public void write(char[] buffer, int offset, int length) {
    for (int i = 0; i < length; i++) {
      write(buffer[offset + i]);
    }
  }

  @Override
  public void flush() {}

  @Override
  public void close() {}
}
