/*
 * Created on 2005-10-09
 */

package traffix.core.sim.generation;

import java.util.HashSet;
import java.util.Set;

import traffix.Traffix;
import traffix.core.accident.IAccidentModel;
import traffix.core.accident.IAccidentParticipant;
import traffix.core.sim.entities.IMobile;

public class AccidentGenerationModel implements IGenerationModel {
  private Set<IAccidentParticipant> m_currentParts = new HashSet<IAccidentParticipant>();

  public AccidentGenerationModel() {
  }

  public int getNumQueued() {
    return 0;
  }

  public void reset() {
    m_currentParts.clear();
  }

  public void rewind() {
    m_currentParts.clear();
  }

  public void update(float t0, float delta) {
    IAccidentModel accModel = Traffix.model().getActiveAccident();
    for (IAccidentParticipant p : accModel.participants()) {
      if (p.getArriveTime() <= t0 - delta && !m_currentParts.contains(p)) {
        m_currentParts.add(p);
        IMobile m = p.createMobile();
        Traffix.simManager().addMobile(m);
      }
    }
  }
}
