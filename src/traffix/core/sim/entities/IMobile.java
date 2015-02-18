/*
 * Created on 2004-08-24
 */

package traffix.core.sim.entities;

import org.tw.geometry.Obb;
import org.tw.geometry.Vec2f;
import org.tw.geometry.Polygonf;
import traffix.core.sim.IObstacle;
import traffix.core.sim.Route;
import traffix.core.sim.graph.GraphLocation;

public interface IMobile {
  public static final int Bus = 2;
  public static final int HeavyVehicle = 1;
  public static final int NormalVehicle = 0;
  public static final int Trolley = 3;
  public static final int Pedestrian = 4;
  public static final int Cyclist = 5;
  public static final int NumTypes = 6;

  public static class MoveParams implements Cloneable {
    public float acceleration;
    public float length;
    public float speed; // in meters per second
    public float vdelta; // in meters per second

    public void assign(MoveParams params) {
      speed = params.speed;
      acceleration = params.acceleration;
      length = params.length;
      vdelta = params.vdelta;
    }

    @Override
    public MoveParams clone() {
      try {
        MoveParams res = (MoveParams) super.clone();
        return res;
      } catch (CloneNotSupportedException e) {
        e.printStackTrace();
        return null;
      }
    }
  }

  float approxNodeImpactTime(traffix.core.sim.graph.Node node);
  float approxTravelTime(float dist);

  boolean bumpedIntoObstacle();
  void dispose();
  float getArrivalTime();
  Polygonf getBounds();
  Vec2f getDirection();
  float getDistanceToObstacle();
  float getLength();
  float getLocation();
  GraphLocation getMobileGraphLocation();
  Obb getObb();
  Vec2f getPosition();
  Route getRoute();
  float getSpeed();
  int getType();
  int getVirtualVehiclesWeight();
  void init();
  boolean isBlocked(IObstacle except);
  boolean isBlockedByLight();
  void limitSpeed(float maxspeed);
  boolean shouldDisintegrate();
  void stopFor(float time);
  void tick(float t0, float delta);
}