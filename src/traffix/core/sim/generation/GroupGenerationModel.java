/*
 * Created on 2004-08-28
 */

package traffix.core.sim.generation;

import traffix.Traffix;
import traffix.core.schedule.Schedule;
import traffix.core.sim.Route;

import java.util.List;
import java.util.Random;

public class GroupGenerationModel extends AbstractGenerationModel {
  private static Random s_random = new Random();
  private float m_numCycles;
  private int m_scheduleLen;

  public float chooseTime(Route route) {
    int prefSec = route.getInfo().getPreferredSec() - 1;
    float t = 0;
    if (prefSec == -1)
      t = (float) (Math.random()*3600);
    else
      t = route.getInfo().getPreferredSec() + s_random.nextInt(Math.round(m_numCycles))*m_scheduleLen;
    if (t >= 3600)
      t = 3600;
    if (t < 0)
      t = 0;
    return t;
  }

  public List<Entry> generateHourlySchedule(List<Route> routes) {
    Schedule sch = Traffix.simManager().getActiveSchedule();
    m_scheduleLen = 0;
    if (sch != null)
      m_scheduleLen = sch.getProgramLength();
    else
      m_scheduleLen = 80;
    m_numCycles = 3600.0f/m_scheduleLen;

    return super.generateHourlySchedule(routes);
  }
}