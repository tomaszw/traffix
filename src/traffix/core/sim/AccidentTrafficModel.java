/*
 * Created on 2005-10-09
 */

package traffix.core.sim;

import traffix.Traffix;
import traffix.core.accident.IAccidentModel;
import traffix.core.sim.entities.Mobile;
import traffix.core.sim.graph.GraphLocation;

public class AccidentTrafficModel extends StandardTrafficModel {

  @Override
  public void move(Mobile mobile, float delta) {
    IAccidentModel accModel = Traffix.model().getActiveAccident();
    GraphLocation loc = mobile.getMobileGraphLocation();
    float t = loc.getDistanceOverEdge() / loc.getEdge().getLength();
    float as = accModel.getNodeSpeed(loc.getEdge().A);
    float bs = accModel.getNodeSpeed(loc.getEdge().B);
    
    float dist = loc.getEdge().getLength() - loc.getDistanceOverEdge();
    float remTime = dist/mobile.speed;
    float desiredV = dist/remTime;
    if (desiredV == Float.NaN)
      desiredV = Float.POSITIVE_INFINITY;
    mobile.limitSpeed = desiredV;
    
    float stopTime = accModel.getNodeStopTime(loc.getEdge().A);
    if (stopTime != 0) {
      if (!accModel.didStopAlready(loc.getEdge().A, mobile)) {
        accModel.makeCoffeBreak(loc.getEdge().A, stopTime, mobile);
        return;
      }
    }
    super.move(mobile, delta);
  }
}
