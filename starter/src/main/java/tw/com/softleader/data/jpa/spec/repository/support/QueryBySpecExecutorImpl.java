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
