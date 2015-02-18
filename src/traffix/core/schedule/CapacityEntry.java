
package traffix.core.schedule;

import traffix.core.VehicleGroup;
import traffix.Traffix;

/**
 * Author: tomek
 * Date: 2005-02-27
 */
public class CapacityEntry implements Cloneable {
  public static final int ROAD_F = 0;
  public static final int ROAD_FL = 1;
  public static final int ROAD_FR = 2;

  public int Q;
  public float Cp;
  public float QCp;
  public float dCp;
  public int S;

  @Override
  public CapacityEntry clone() {
    try {
      CapacityEntry cloned = (CapacityEntry) super.clone();
      return cloned;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }

  public boolean equals(Object o) {
    if (!(o instanceof CapacityEntry))
      return false;
    CapacityEntry e = (CapacityEntry) o;
    // all fields except Q are derived data
    return
      Q == e.Q;
//    &&
  //    Cp == e.Cp &&
    //  QCp == e.QCp &&
     // S == e.S &&
     // dCp == e.dCp;
  }
}
