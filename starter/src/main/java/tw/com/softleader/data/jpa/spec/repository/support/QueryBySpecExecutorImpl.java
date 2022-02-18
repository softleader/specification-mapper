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
package tw.com.softleader.data.jpa.spec.repository.support;

import static lombok.AccessLevel.PROTECTED;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import tw.com.softleader.data.jpa.spec.SpecMapper;
import tw.com.softleader.data.jpa.spec.repository.QueryBySpecExecutor;

/**
 * Default implementation for {@code QueryBySpecExecutor}
 *
 * @author Matt Ho
 */
public class QueryBySpecExecutorImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
    implements QueryBySpecExecutor<T> {

  @Setter
  @Getter(PROTECTED)
  private SpecMapper specMapper;

  public QueryBySpecExecutorImpl(
      @NonNull JpaEntityInformation<T, ?> entityInformation,
      @NonNull EntityManager entityManager) {
    super(entityInformation, entityManager);
  }

  @Override
  @Transactional(readOnly = true)
  public List<T> findBySpec(Object spec) {
    return findAll(getSpecMapper().toSpec(spec, getDomainClass()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<T> findBySpec(Object spec, @NonNull Sort sort) {
    return findAll(getSpecMapper().toSpec(spec, getDomainClass()), sort);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<T> findBySpec(Object spec, @NonNull Pageable pageable) {
    return findAll(getSpecMapper().toSpec(spec, getDomainClass()), pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public long countBySpec(Object spec) {
    return count(getSpecMapper().toSpec(spec, getDomainClass()));
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsBySpec(Object spec) {
    return count(getSpecMapper().toSpec(spec, getDomainClass())) > 0;
  }
}
