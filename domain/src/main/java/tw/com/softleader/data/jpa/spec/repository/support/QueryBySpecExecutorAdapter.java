package tw.com.softleader.data.jpa.spec.repository.support;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import tw.com.softleader.data.jpa.spec.SpecMapper;
import tw.com.softleader.data.jpa.spec.repository.QueryBySpecExecutor;

/**
 * Spring Data JAP 的 Query By Spec 擴充 Adapter
 *
 * @author Matt Ho
 */
@NoRepositoryBean
public interface QueryBySpecExecutorAdapter<T> extends
    QueryBySpecExecutor<T>,
    JpaSpecificationExecutor<T> {

  SpecMapper getSpecMapper();

  Class<T> getGenericType();

  @Override
  default List<T> findBySpec(Object spec) {
    return findAll(getSpecMapper().toSpec(spec, getGenericType()));
  }

  @Override
  default List<T> findBySpec(Object spec, Sort sort) {
    return findAll(getSpecMapper().toSpec(spec, getGenericType()), sort);
  }

  @Override
  default Page<T> findBySpec(Object spec, Pageable pageable) {
    return findAll(getSpecMapper().toSpec(spec, getGenericType()), pageable);
  }

  @Override
  default long countBySpec(Object spec) {
    return count(getSpecMapper().toSpec(spec, getGenericType()));
  }

  @Override
  default boolean existsBySpec(Object spec) {
    return countBySpec(spec) > 0;
  }
}
