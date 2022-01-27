package tw.com.softleader.data.jpa.spec.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Interface to allow execution of Query by Spec instances.
 *
 * @author Matt Ho
 */
@NoRepositoryBean
public interface QueryBySpecExecutor<T> {

  List<T> findBySpec(Object spec);

  List<T> findBySpec(Object spec, Sort sort);

  Page<T> findBySpec(Object spec, Pageable pageable);

  long countBySpec(Object spec);

  boolean existsBySpec(Object spec);
}
