/*
 * Created on 2004-08-31
 */

package traffix.core.sim;

import traffix.core.sim.entities.IEntity;
import traffix.core.sim.entities.IMobile;
import traffix.core.sim.graph.GraphLocation;

import java.util.List;


public abstract class AbstractObstacle implements IObstacle, IEntity {
  protected GraphLocation[] m_obstacleLocations = new GraphLocation[0];

  public GraphLocation[] getObstacleGraphLocations() {
    return m_obstacleLocations;
  }

  public void notifyApproach(IMobile mobile, float remainingDist) {
  }

  public void notifyPass(IMobile mobile) {
  }

  public void dispose() {
    unregisterLocations();
    m_obstacleLocations = new GraphLocation[0];
  }

  protected void updateGraphLocations() {
    unregisterLocations();
    List<GraphLocation> locs = computeObstacleLocations();
    m_obstacleLocations = locs.toArray(new GraphLocation[locs.size()]);
    registerLocations();
  }

  protected abstract List<GraphLocation> computeObstacleLocations();

  private void registerLocations() {
    for (int i = 0; i < m_obstacleLocations.length; i++) {
      m_obstacleLocations[i].getEdge().addObstacle(this);
    }
  }

  private void unregisterLocations() {
    for (int i = 0; i < m_obstacleLocations.length; i++) {
      m_obstacleLocations[i].getEdge().removeObstacle(this);
    }
  }

}
