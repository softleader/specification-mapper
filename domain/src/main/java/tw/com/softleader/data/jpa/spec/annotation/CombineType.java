package tw.com.softleader.data.jpa.spec.annotation;

import org.springframework.data.jpa.domain.Specification;

/**
 * @author Matt Ho
 */
public enum CombineType {
  /**
   * Respecting the default combine algorithm
   */
  RESPECT,
  /**
   * Force using {@link Specification#and(Specification)} to combine Specs
   */
  AND,
  /**
   * Force using {@link Specification#or(Specification)} to combine Specs
   */
  OR
}
