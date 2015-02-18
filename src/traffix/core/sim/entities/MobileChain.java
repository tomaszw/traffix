/*
 * Created on 2004-09-01
 */

package traffix.core.sim.entities;

import org.tw.geometry.*;
import traffix.Traffix;
import traffix.core.sim.ITrafficModelProvider;
import traffix.core.sim.graph.GraphLocation;

import java.util.List;

public class MobileChain extends Mobile implements IMobileChain {
  public MobileChain prevInChain, nextInChain;

  public MobileChain(ITrafficModelProvider tmp) {
    super(tmp);
  }

  public boolean countsAsSingle() {
    return prevInChain == null;
  }

  @Override
  public void dispose() {
    super.dispose();
    MobileChain m = nextInChain;
    while (m != null) {
      m.dispose();
      m = m.nextInChain;
    }
  }
  
  @Override
  public void tick(float t0, float delta) {
    super.tick(t0, delta);
    MobileChain m = nextInChain;
    while (m != null) {
      m.tick(t0, delta);
      m = m.nextInChain;
    }
  }
  
  public boolean shouldDisintegrate() {
    if (isFirst())
      return super.shouldDisintegrate();
    else
      return getPrevInChain().shouldDisintegrate();
  }

  public int getVirtualVehiclesWeight() {
    return isFirst() ? super.getVirtualVehiclesWeight() : 0;
  }

  public boolean isFirst() {
    return getPrevInChain() == null;
  }

  public IMobileChain getNextInChain() {
    return nextInChain;
  }

  public IMobileChain getPrevInChain() {
    return prevInChain;
  }

  public float getDistanceToObstacle() {
    if (getPrevInChain() != null)
      return Float.POSITIVE_INFINITY;
    return super.getDistanceToObstacle();
  }

  protected List<GraphLocation> computeObstacleLocations() {
    List<GraphLocation> res = super.computeObstacleLocations();
    IMobileChain next = getNextInChain();
    while (next != null) {
      res.addAll(((MobileChain)next).computeObstacleLocations());
      next = next.getNextInChain();
    }
    
    if (getPrevInChain() != null) {
      // link hook is obstacle as well
      Rectanglef clip = new Rectanglef();
      Segmentf seg = new Segmentf();
      seg.a = getPosition();
      seg.b = seg.a.add(getDirection());
      float cx1, cy1, cx2, cy2;
      cx1 = Math.min(seg.a.x, seg.b.x);
      cx2 = Math.max(seg.a.x, seg.b.x);
      cy1 = Math.min(seg.a.y, seg.b.y);
      cy2 = Math.max(seg.a.y, seg.b.y);
      clip.x = cx1;
      clip.y = cy1;
      clip.width = cx2 - cx1;
      clip.height = cy2 - cy1;
      res.addAll(Traffix.simManager().getGraph().intersectSegment(seg,
        clip));
    }
    return res;
  }

  protected void updateObb() {
    if (getPrevInChain() == null) {
      super.updateObb();
    } else if (getPrevInChain() != null) {
      Polygonf bounds = new Polygonf();
      for (int i = 0; i < 4; ++i) {
        bounds.addPoint(new Vec2f());
      }
      float width = 2;
      bounds.getPoint(0).assign(-length, -width/2);
      bounds.getPoint(1).assign(1.5f, -width/2);
      bounds.getPoint(2).assign(1.5f, width/2);
      bounds.getPoint(3).assign(-length, width/2);
      Vec2f frontPos = getPosition();
      Vec2f zero = new Vec2f(0, 0);
      for (int i = 0; i < bounds.getNumSides(); ++i) {
        Vec2f p = bounds.getPoint(i);
        bounds.setPoint(i, p.rotate(zero, rotation).add(frontPos));
      }
      Vec2f[] pts = new Vec2f[4];
      for (int i = 0; i < 4; ++i)
        pts[i] = bounds.getPoint(i);
      m_obb = new Obb(pts);
    }
  }
}