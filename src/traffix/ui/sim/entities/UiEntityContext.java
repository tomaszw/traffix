/*
 * Created on 2004-07-23
 */

package traffix.ui.sim.entities;

import traffix.Traffix;
import traffix.core.sim.ISimManager;
import traffix.ui.Gc;
import traffix.ui.sim.CoordinateTransformer;
import traffix.ui.sim.IMapEditor;

public class UiEntityContext {
  public boolean filming = false;
  public boolean informationFrame = false;
  public IMapEditor mapEditor = null;
  public boolean simRunning = false;
  
  private CoordinateTransformer m_ctTransformer;
  private Gc m_mapGc;

  public ISimManager getSimManager() {
    return Traffix.simManager();
  }
  
  public CoordinateTransformer getCoordTransformer() {
    return m_ctTransformer;
  }

  public Gc getGc() {
    return m_mapGc;
  }

  public void setCoordTransformer(CoordinateTransformer ct) {
    m_ctTransformer = ct;
  }

  public void setGc(Gc gc) {
    m_mapGc = gc;
  }
}