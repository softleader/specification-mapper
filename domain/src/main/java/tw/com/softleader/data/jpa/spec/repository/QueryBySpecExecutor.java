package tw.com.softleader.data.jpa.spec.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JAP 的 Query By Spec 擴充
 *
 * @author Matt Ho
 */
@NoRepositoryBean
public interface QueryBySpecExecutor<T> {

  @Transactional(readOnly = true)
  List<T> findBySpec(Object spec);

  @Transactional(readOnly = true)
  List<T> findBySpec(Object spec, Sort sort);

  @Transactional(readOnly = true)
  Page<T> findBySpec(Object spec, Pageable pageable);

  @Transactional(readOnly = true)
  long countBySpec(Object spec);

  @Transactional(readOnly = true)
  boolean existsBySpec(Object spec);
}
