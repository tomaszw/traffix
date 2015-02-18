/*
 * Created on 2004-08-18
 */

package traffix.core.sim.generation;

import traffix.core.sim.entities.Mobile;

public interface IGenerationModel {
  public static final int GROUP    = 1;
  public static final int STANDARD = 0;

  public static class Entry {
    public float    absoluteArrivalTime;
    public Mobile[] mobiles;
    public float    prevTryTime;
    public float    relativeArrivalTime;
  }

  int getNumQueued();
  void reset();
  void rewind();
  void update(float t0, float delta);
}