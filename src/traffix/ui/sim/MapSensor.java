/*
 * Created on 2004-08-04
 */

package traffix.ui.sim;

import org.tw.geometry.Vec2f;
import traffix.Traffix;
import traffix.core.sim.entities.*;
import traffix.core.sim.graph.Graph;
import traffix.core.sim.graph.Node;
import traffix.ui.sim.entities.BusStopUi;
import traffix.ui.sim.entities.LightUi;
import traffix.ui.sim.entities.PedestrianPathUi;

import java.util.Iterator;

public class MapSensor {
  private IMapEditor m_editor;
  private float m_threshold = 0;

  public MapSensor(IMapEditor editor) {
    m_editor = editor;
  }

  public Barrier senseBarrier(Vec2f p) {
    return (Barrier) senseStaticEntity(p, Barrier.class, 8);
  }

  public BarrierDetector senseBarrierDetector(Vec2f p) {
    return (BarrierDetector) senseStaticEntity(p, BarrierDetector.class, 8);
  }

  public CondClearDetector senseCondClearDetector(Vec2f p) {
    return (CondClearDetector) senseStaticEntity(p, CondClearDetector.class, 8);
  }

  public BusStop senseBusStop(Vec2f pos) {
    return (BusStop) senseStaticEntity(pos, BusStop.class, BusStopUi.CENTER_RADIUS);
  }

  public Light senseLight(Vec2f pos) {
    return (Light) senseStaticEntity(pos, Light.class, LightUi.CENTER_RADIUS);
  }

  public Node senseNode(Vec2f pos) {
    float dist = Float.MAX_VALUE;
    Node res = null;
    Graph g = Traffix.simManager().getGraph();
    for (Iterator<Node> iter = g.getNodeIterator(); iter.hasNext();) {
      Node node = iter.next();
      float d = node.getPos().distanceTo(pos);
      if (d < dist && d < m_threshold) {
        dist = d;
        res = node;
      }
    }
    return res;
  }

  public Node senseNodeLabel(Vec2f pos) {
    float dist = Float.MAX_VALUE;
    float senseDist = m_editor.getCoordTransformer().screenToTerrain(12);
    Node res = null;
    Graph g = Traffix.simManager().getGraph();
    for (Iterator<Node> iter = g.getNodeIterator(); iter.hasNext();) {
      Node node = iter.next();
      if (node.isBeginningNode()) {
        float d = node.labelPos.distanceTo(pos);
        if (d < dist && d < senseDist) {
          dist = d;
          res = node;
        }
      }
    }
    return res;
  }

  public IEntity sensePedestrianDetector(Vec2f pos) {
    return (PedestrianDetector) senseStaticEntity(pos, PedestrianDetector.class, 8);
  }

  public PedestrianPath sensePedestrianPath(Vec2f pos) {
    return (PedestrianPath) senseStaticEntity(pos, PedestrianPath.class,
      PedestrianPathUi.CENTER_RADIUS);
  }

  public IEntity sensePresenceDetector(Vec2f pos) {
    return (PresenceDetector) senseStaticEntity(pos, PresenceDetector.class, 8);
  }

  public IEntity senseSlowdownDetector(Vec2f pos) {
    return (SlowdownDetector) senseStaticEntity(pos, SlowdownDetector.class, 8);
  }
  
  public IEntity senseStaticEntity(Vec2f pos, Class type, int radius) {
    float minDist = Float.MAX_VALUE;
    IEntity entity = null;
    for (Iterator<IEntity> iter = Traffix.simManager().entityManager().iterator(); iter.hasNext();) {
      IEntity e = iter.next();
      if (e.getClass().equals(type)) {
        float dist = pos.distanceTo(e.getCenter());
        if (dist < minDist) {
          minDist = dist;
          entity = e;
        }
      }
    }

    if (entity != null) {
      if (minDist < m_editor.getCoordTransformer().screenToTerrain(radius)) {
        return entity;
      }
    }
    return null;
  }

  public TransitDetector senseTransitDetector(Vec2f pos) {
    return (TransitDetector) senseStaticEntity(pos, TransitDetector.class, 8);
  }

  public void setThresholdInMeters(float threshold) {
    m_threshold = threshold;
  }

  public void setThresholdInPixels(int threshold) {
    setThresholdInMeters(m_editor.getCoordTransformer().screenToTerrain(threshold));
  }

}