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
package tw.com.softleader.data.jpa.spec.domain;

import java.util.Collection;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
@ToString
public class Conjunction<T> extends CompoundSpecification<T> {

  public Conjunction(@NonNull Collection<Specification<T>> specs) {
    super(specs);
  }

  @Override
  protected Specification<T> combine(Specification<T> result,
      Specification<T> element) {
    if (element instanceof OrSpecification) {
      return result.or(element);
    }
    return result.and(element);
  }

}
