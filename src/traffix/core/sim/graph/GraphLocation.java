/*
 * Created on 2004-08-23
 */

package traffix.core.sim.graph;

import org.tw.geometry.Vec2f;

public class GraphLocation {
  private float distance;
  private float distanceOverEdge;
  private float distanceUptoEdge;
  private GraphEdge edge;
  private Vec2f point;

  public float getDistance() {
    return distance;
  }
  public float getDistanceOverEdge() {
    return distanceOverEdge;
  }
  public float getDistanceUptoEdge() {
    return distanceUptoEdge;
  }
  public GraphEdge getEdge() {
    return edge;
  }
  public Vec2f getPoint() {
    return point;
  }
  public void setDistance(float distance) {
    this.distance = distance;
  }
  public void setDistanceOverEdge(float distanceOverEdge) {
    this.distanceOverEdge = distanceOverEdge;
  }
  public void setDistanceUptoEdge(float distanceUptoEdge) {
    this.distanceUptoEdge = distanceUptoEdge;
  }
  public void setEdge(GraphEdge edge) {
    this.edge = edge;
  }
  public void setPoint(Vec2f point) {
    this.point = point;
  }
}