
package traffix.core.sim.entities;

/**
 * Author: tomek
 * Date: 2005-07-11
 */
public class FreezeLink {
  public IDetector linkedDetector;
  public int tmax;

  public void assign(FreezeLink l) {
    linkedDetector = l.linkedDetector;
    tmax = l.tmax;
  }

  // auxiliary info for loading data
  public String linkedDetectorName;
}
