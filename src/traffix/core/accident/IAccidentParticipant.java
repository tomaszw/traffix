/*
 * Created on 2005-09-21
 */

package traffix.core.accident;

import org.eclipse.swt.graphics.RGB;
import org.tw.geometry.Vec2f;

import traffix.core.sim.entities.IMobile;
import traffix.core.sim.graph.Node;

public interface IAccidentParticipant {
  IAccidentParticipant clone();
  IMobile createMobile();
  Node getArriveNode();
  float getArriveTime();
  RGB getColor();
  String getName();
  IAccidentPath getPath();
  Vec2f getPos();
  APType getType();

  void setArriveNode(Node n);
  void setArriveTime(float t);
  void setColor(RGB clr);
  void setName(String name);
  void setPath(IAccidentPath path);
  void setType(APType t);
}
