/*
 * Created on 2004-08-23
 */

package traffix.core.sim;

import org.tw.geometry.Polygonf;
import traffix.core.sim.entities.IMobile;
import traffix.core.sim.graph.GraphLocation;

public interface IObstacle {
  Polygonf getBounds();
  GraphLocation[] getObstacleGraphLocations();
  float getObstacleRadius();
  void notifyApproach(IMobile mobile, float remainingDist);
  void notifyPass(IMobile mobile);
  boolean shouldBlock(IMobile mobile);
}