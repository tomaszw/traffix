/*
 * Created on 2004-08-20
 */

package traffix.core.sim.generation;

import traffix.core.sim.Route;


public class StandardGenerationModel extends AbstractGenerationModel {
  public float chooseTime(Route route) {
    return (float) (Math.random()*3600);
  }
}