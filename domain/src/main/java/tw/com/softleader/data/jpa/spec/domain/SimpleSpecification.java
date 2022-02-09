package tw.com.softleader.data.jpa.spec.domain;

import lombok.NonNull;

/**
 * The constraint of constructor
 *
 * @author Matt Ho
 */
public abstract class SimpleSpecification<T> extends PathSpecification<T> {

  protected final Object value;

  protected SimpleSpecification(@NonNull Context context, @NonNull String path,
      @NonNull Object value) {
    super(context, path);
    this.value = value;
  }
}
