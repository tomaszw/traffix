/*
 * Created on 2004-08-29
 */

package traffix.core.sim.graph;

import org.tw.geometry.Vec2f;
import traffix.core.sim.IObstacle;
import traffix.core.sim.entities.IMobile;

public interface IGraphPath {

  public static final class ObstacleEntry {
    public IObstacle     obstacle;
    public GraphLocation pathLocation;
  }

  // void add(int index, Node node);
  // void add(Node node);
  // IGraphPath chopUpto(Node end);
  
  boolean containsEdge(Node a, Node b);
  float getDistanceTo(Vec2f p);

  IGraphPath.ObstacleEntry getFirstBlockingObstacle(float since, IMobile caller);

  IGraphPath.ObstacleEntry getFirstObstacle(float since, IObstacle except);

  float getLength();

  Node getNode(int i);

  int getNodeIndex(Node n);

  float getNodeLocation(Node n);

  // Iterator getNodeIterator();
  int getNumNodes();

  void notifyObstaclesOnApproach(float pos, IMobile mobile);

  // Edge getTraversedEdge();
  Vec2f traverseDistance(float loc);

  GraphLocation traverseDistanceEx(float loc);

  Vec2f traversePercentage(float per);

  IGraphPath trim(float min, float max);
  // IGraphPath trim(float maxDistance);
  void update();
}