/*
 * Created on 2005-09-24
 */

package traffix.core;

public class Pair<A, B> {
  public Pair() {
  }
  
  public Pair(A a, B b) {
    this.a = a;
    this.b = b;
  }
  
  public A a;
  public B b;
}
