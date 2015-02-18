/*
 * Created on 2004-07-05
 */

package traffix.core.model;

public class Bitset {
  private boolean[] m_bits;

  public Bitset(int size) {
    m_bits = new boolean[size];
  }

  public Bitset(int size, int[] setBits) {
    m_bits = new boolean[size];
    for (int i = 0; i < setBits.length; ++i)
      m_bits[setBits[i]] = true;
  }

  public boolean any() {
    for (int i = 0; i < m_bits.length; ++i)
      if (m_bits[i])
        return true;
    return false;
  }

  public boolean test(int pos) {
    return m_bits[pos];
  }

  public int count() {
    int r = 0;
    for (int i = 0; i < m_bits.length; ++i)
      if (m_bits[i])
        ++r;
    return r;
  }

  public int size() {
    return m_bits.length;
  }

  public boolean none() {
    return count() == 0;
  }

  public void flip(int pos) {
    m_bits[pos] = !m_bits[pos];
  }

  public void flip() {
    for (int i = 0; i < m_bits.length; ++i)
      m_bits[i] = !m_bits[i];
  }

  public void reset(int pos) {
    m_bits[pos] = false;
  }

  public void reset() {
    for (int i = 0; i < m_bits.length; ++i)
      m_bits[i] = false;
  }

  public void set(int pos) {
    m_bits[pos] = true;
  }

  public void set() {
    for (int i = 0; i < m_bits.length; ++i)
      m_bits[i] = true;
  }

  public Bitset and(Bitset rhs) {
    Bitset r = new Bitset(size());
    for (int i = 0; i < m_bits.length; ++i)
      r.m_bits[i] = m_bits[i] & rhs.m_bits[i];
    return r;
  }

  public Bitset or(Bitset rhs) {
    Bitset r = new Bitset(size());
    for (int i = 0; i < m_bits.length; ++i)
      r.m_bits[i] = m_bits[i] | rhs.m_bits[i];
    return r;
  }

  public Bitset xor(Bitset rhs) {
    Bitset r = new Bitset(size());
    for (int i = 0; i < m_bits.length; ++i)
      r.m_bits[i] = m_bits[i] ^ rhs.m_bits[i];
    return r;
  }
}