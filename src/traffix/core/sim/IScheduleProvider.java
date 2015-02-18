/*
 * Created on 2004-08-20
 */

package traffix.core.sim;

import traffix.core.schedule.Schedule;

public interface IScheduleProvider {
  Schedule getActiveSchedule();
}
