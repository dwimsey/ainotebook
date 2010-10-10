
package flj;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the categories of a {@link fj.test.Property property}, which are the union of
 * categories specified on the enclosing class and the categories specified on the method or field
 * that make up the property.
 *
 * @version 2.20<br>
 *          <ul>
 *          <li>$LastChangedRevision: 5 $</li>
 *          <li>$LastChangedDate: 2008-12-06 16:49:43 +1000 (Sat, 06 Dec 2008) $</li>
 *          <li>$LastChangedBy: tonymorris $</li>
 *          </ul>
 */
@Documented
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Category {
  /**
   * The categories of the property.
   *
   * @return The categories of the property.
   */
  String[] value();
}
