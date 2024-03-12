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

import static java.lang.String.copyValueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Writer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

/**
 * This interface is used to create writer for AST.
 *
 * @author Matt Ho
 */
public interface WriterFactory {

  /**
   * Create a writer for the given root object and specification.
   *
   * @param rootObject The target object to be mapped, never null
   * @param spec The specification to be mapped, cloud be null
   */
  Writer createWriter(@NonNull Object rootObject, @Nullable Specification<Object> spec);

  static WriterFactory domainWriterFactory() {
    return (rootObject, spec) -> new Slf4jDebugWriter(getLogger(SpecMapper.class));
  }

  static WriterFactory impersonateWriterFactory() {
    return (rootObject, spec) -> new Slf4jDebugWriter(getLogger(rootObject.getClass()));
  }
}

@RequiredArgsConstructor
class Slf4jDebugWriter extends Writer {

  @NonNull public final Logger logger;

  @Override
  public void write(char[] cbuf, int off, int len) {
    if (!logger.isDebugEnabled()) {
      return;
    }
    // Remove the end of line chars
    while (len > 0 && (cbuf[len - 1] == '\n' || cbuf[len - 1] == '\r')) {
      len--;
    }
    logger.debug(copyValueOf(cbuf, off, len));
  }

  @Override
  public void flush() {
    // no-op
  }

  @Override
  public void close() {
    // no-op
  }
}
