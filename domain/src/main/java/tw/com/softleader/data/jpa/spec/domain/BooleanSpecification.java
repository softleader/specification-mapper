package tw.com.softleader.data.jpa.spec.domain;

import lombok.NonNull;

/**
 * A convenience class to check and get the value of {@code Boolean} type
 *
 * @author Matt Ho
 */
abstract class BooleanSpecification<T> extends SimpleSpecification<T> {

  public BooleanSpecification(@NonNull Context context, @NonNull String path,
      @NonNull Object value) {
    super(context, path, value);
    if (!(value instanceof Boolean)) {
      throw new TypeMismatchException(value, Boolean.class);
    }
  }

  protected boolean getValue() {
    return (Boolean) value;
  }
}
