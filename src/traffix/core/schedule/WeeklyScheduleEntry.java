/*
 * Created on 2004-07-13
 */

package traffix.core.schedule;


public class WeeklyScheduleEntry implements Cloneable {
  public int day, hour, minute, offset;
  public String scheduleName;
  public int junctionNum = 0;
  
  @Override
  public WeeklyScheduleEntry clone() {
    try {
      WeeklyScheduleEntry cloned = (WeeklyScheduleEntry) super.clone();
      return cloned;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  } 
}