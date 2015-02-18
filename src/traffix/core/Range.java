/*
 * Created on 2004-07-10
 */

package traffix.core;

public final class Range {
  private int m_from;
  private int m_to;

  public Range(int from, int to) {
    m_from = from;
    m_to = to;
  }
  public int from() {
    return m_from;
  }
  public int to() {
    return m_to;
  }
}