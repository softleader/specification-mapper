package tw.com.softleader.data.jpa.spec.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Interface to allow execution of Query by Spec instances.
 *
 * @author Matt Ho
 */
@NoRepositoryBean
public interface QueryBySpecExecutor<T> {

  List<T> findBySpec(@Nullable Object spec);

  List<T> findBySpec(@Nullable Object spec, @NonNull Sort sort);

  Page<T> findBySpec(@Nullable Object spec, @NonNull Pageable pageable);

  long countBySpec(@Nullable Object spec);

  boolean existsBySpec(@Nullable Object spec);
}
