package tw.com.softleader.data.jpa.spec.domain;

import lombok.NonNull;

/**
 * A convenience class to check and get the value of {@code Comparable} type
 *
 * @author Matt Ho
 */
abstract class ComparableSpecification<T> extends SimpleSpecification<T> {

  public ComparableSpecification(@NonNull Context context, @NonNull String path,
      @NonNull Object value) {
    super(context, path, value);
    if (!(value instanceof Comparable)) {
      throw new TypeMismatchException(value, Comparable.class);
    }
  }

  protected Comparable getValue() {
    return ((Comparable) value);
  }
}
