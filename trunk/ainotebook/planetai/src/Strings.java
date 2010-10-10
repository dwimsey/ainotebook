


/**
 * Curried string functions.
 *
 * @version 2.20<br>
 *          <ul>
 *          <li>$LastChangedRevision: 5 $</li>
 *          <li>$LastChangedDate: 2008-12-06 16:49:43 +1000 (Sat, 06 Dec 2008) $</li>
 *          </ul>
 */
public class Strings {
  private Strings() {
    throw new UnsupportedOperationException();
  }

  /**
   * A curried version of {@link String#isEmpty()}.
   */
  public static final F<String, Boolean> isEmpty = new F<String, Boolean>() {
    public Boolean f(final String s) {
      return s.length() == 0;
    }
  };

  /**
   * A curried version of {@link String#length()}.
   */
  public static final F<String, Integer> length = new F<String, Integer>() {
    public Integer f(final String s) {
      return s.length();
    }
  };

  /**
   * A curried version of {@link String#contains(CharSequence)}.
   * The function returns true if the second argument contains the first.
   */
  public static final F<String, F<String, Boolean>> contains = Function.curry(new F2<String, String, Boolean>() {
    public Boolean f(final String s1, final String s2) {
      return s2.contains(s1);
    }
  });

  /**
   * A curried version of {@link String#matches(String)}.
   * The function returns true if the second argument matches the first.
   */
  public static final F<String, F<String, Boolean>> matches = Function.curry(new F2<String, String, Boolean>() {
    public Boolean f(final String s1, final String s2) {
      return s2.matches(s1);
    }
  });

}
