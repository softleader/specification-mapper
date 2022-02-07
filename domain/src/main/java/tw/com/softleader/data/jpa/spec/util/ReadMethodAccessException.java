package tw.com.softleader.data.jpa.spec.util;

/**
 * Exceptions related to accessing read method
 *
 * @author Matt Ho
 */
public class ReadMethodAccessException extends RuntimeException {

  public ReadMethodAccessException(String message, Throwable cause) {
    super(message, cause);
  }
}
