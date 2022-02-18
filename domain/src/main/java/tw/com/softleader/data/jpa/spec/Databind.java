/*
 * Copyright © 2022 SoftLeader (support@softleader.com.tw)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tw.com.softleader.data.jpa.spec;

import java.lang.reflect.Field;
import java.util.Optional;
import org.springframework.lang.NonNull;

/**
 * Databind 抽象了每個物件中的欄位, 讓怎麼取得欄位的方式有機會抽換成效能更好的實作
 *
 * @author Matt Ho
 */
public interface Databind {

  /**
   * 欄位所在的物件
   */
  @NonNull
  Object getTarget();

  /**
   * 欄位
   */
  @NonNull
  Field getField();

  /**
   * 欄位值
   */
  Optional<Object> getFieldValue();
}
