/*
 * Created on 2004-08-18
 */

package traffix.core.sim;

import traffix.core.sim.entities.Mobile;


public interface ITrafficModel {
  void move(Mobile vehicle, float delta);
}
