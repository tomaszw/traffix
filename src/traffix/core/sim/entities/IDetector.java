
package traffix.core.sim.entities;

/**
 * Author: tomek
 * Date: 2005-07-11
 */
public interface IDetector {
  int getState();
  String getName();
  boolean isActive();
  boolean isReactingToMobileType(int type);
  void setReactingToMobileType(int type, boolean r);
  void postLoadFix();
}
