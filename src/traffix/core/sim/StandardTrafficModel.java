/*
 * Created on 2004-08-21
 */

package traffix.core.sim;

import java.util.Random;

import traffix.core.sim.entities.IMobileChain;
import traffix.core.sim.entities.IMobile;
import traffix.core.sim.entities.Mobile;

public class StandardTrafficModel implements ITrafficModel {
  private float m_Lmax = 10;
  private float m_Lmin = 0.25f;
  private float m_Rmax = (float) Math.PI/2;
  private float m_Rmin = (float) Math.PI/32;

  public void move(Mobile mobile, float delta) {
    IMobileChain chained = null;
    if (mobile instanceof IMobileChain)
      chained = (IMobileChain) mobile;

    if (chained != null && !chained.isFirst() &&
      !chained.getPrevInChain().shouldDisintegrate()) {
      IMobileChain prev = chained.getPrevInChain();
      mobile.speed = prev.getSpeed();
      mobile.location = prev.getLocation() - prev.getLength() - 1;
      if (mobile.location < 0)
        mobile.location = 0;
      return;
    }

    float decelDist;
    float accel = mobile.maxAccel;
    if (mobile.getFirstBlockingObstacle() != null &&
        mobile.getFirstBlockingObstacle() instanceof IMobile) {
      // less safety margins if obstacle is another mobile
      decelDist = calcDecelerationDistance(mobile.maxSpeed, accel*4);
    } else {
      decelDist = calcDecelerationDistance(mobile.maxSpeed, accel*2);
    }
    m_Lmax = m_Lmin + decelDist;
    float d = mobile.getDistanceToObstacle();
    mobile.targetSpeed = calcSpeed(mobile, d);

    if (mobile.targetSpeed < mobile.speed) {
      mobile.speed = mobile.targetSpeed;
//      mobile.speed -= 8 * mobile.maxAccel * delta; //mobile.targetSpeed;
//      if (mobile.speed < mobile.targetSpeed)
//        mobile.speed = mobile.targetSpeed;
      if (mobile.speed < 0.5f)
        mobile.speed = 0;

    }
    if (mobile.limitSpeed < mobile.speed) {
      mobile.speed -= accel*2*delta;
      if (mobile.speed < mobile.limitSpeed)
        mobile.speed = mobile.limitSpeed;
    }
    else if (mobile.targetSpeed > mobile.speed) {
      mobile.speed += accel*delta;
      if (mobile.speed > mobile.targetSpeed)
        mobile.speed = mobile.targetSpeed;
    }

    if (mobile.bumpedIntoObstacle())
      mobile.speed = 0;
    mobile.location += mobile.speed*delta;
  }

  private float calcSpeed(Mobile mobile, float d) {
    float speed = 0;
    if (d <= m_Lmin)
      speed = 0;
    else if (d >= m_Lmax)
      speed = mobile.maxSpeed;
    else
      speed = ((d - m_Lmin)/(m_Lmax - m_Lmin))*mobile.maxSpeed;
    float ts = 1;//(1-mobile.getTurnSharpness());
    ts = Math.max(ts, 0.05f);
    return speed*ts;
//    float t = mobile.getTurningSpeed()*2;
//    t = Math.min(1,t);
//    return speed * (1-t);
    // speed should be lower when doing turns!
//    float diff1 = Math.abs(mobile.targetRotation - mobile.rotation);
//    float diff2 = Math.abs(mobile.targetRotation - mobile.rotation + 2*(float)Math.PI);
//    float rotDiff = Math.min(diff1,diff2);
//    float scale;
//    if (rotDiff < m_Rmin)
//      scale = 1;
//    else if (rotDiff > m_Rmax)
//      scale = 0.1f;
//    else
//      scale = (1-((rotDiff - m_Rmin) / (m_Rmax-m_Rmin))) * 0.9f + 0.1f;
//    
//    return speed*scale;
  }

  
  private float calcDecelerationDistance(float v0, float decel) {
    float T = v0/decel;
    return Math.max(0, v0*T - decel*T*T/2);
  }
}