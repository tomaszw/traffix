/*
 * Created on 2004-08-30
 */

package traffix.core.sim;

public class Measure implements Cloneable {
  // buses per hour
  public int busPh;
  // heavy per hour
  public int hPh;
  // normal per hour
  public int nPh;
  // trolelys per hour
  public int trolleyPh;
  // pedestrian show-up interval
  public int pedeInterval;
  // cyclist show-up interval
  public int cyclistInterval;
  
  public void assign(Measure m) {
    nPh = m.nPh;
    hPh = m.hPh;
    busPh = m.busPh;
    trolleyPh = m.trolleyPh;
    pedeInterval = m.pedeInterval;
    cyclistInterval = m.cyclistInterval;
  }

  @Override
  public Measure clone() {
    try {
      Measure res = (Measure) super.clone();
      return res;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }
}
