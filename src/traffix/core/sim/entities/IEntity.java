/*
 * Created on 2004-09-01
 */

package traffix.core.sim.entities;

import org.tw.geometry.Vec2f;
import traffix.core.model.IPersistent;


public interface IEntity extends IPersistent {
  void update();

  void tick(float t0, float delta);

  void reset();

  void dispose();

  Vec2f getCenter();
}
