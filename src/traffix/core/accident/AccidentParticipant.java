/*
 * Created on 2005-09-21
 */

package traffix.core.accident;

import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.tw.geometry.Vec2f;

import traffix.Traffix;
import traffix.core.sim.ISimManager;
import traffix.core.sim.Route;
import traffix.core.sim.entities.IMobile;
import traffix.core.sim.entities.Mobile;
import traffix.core.sim.entities.MobileChain;
import traffix.core.sim.graph.Node;

public class AccidentParticipant implements IAccidentParticipant, Cloneable {
  private Node   m_arriveNode=null;
  private float  m_arriveTime=0;
  private RGB    m_clr = new RGB(0, 0, 255);
  private String m_name="";
  private APType m_type = APType.Car;

  @Override
  public AccidentParticipant clone() {
    try {
      AccidentParticipant res = (AccidentParticipant) super.clone();
      return res;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }

  public IMobile createMobile() {
    int mtype = IMobile.NormalVehicle;
    Node arriveAt = getArriveNode();
    if (arriveAt == null)
      return null;
    List<Route> routes = arriveAt.getRoutesFromNode();
    Route route = routes.get(0);
    assert route != null;
    return doCreateMobile(route, mtype);
  }
  
  private void setupSingleMobile(Route route, int type, Mobile mobile) {
    mobile.type = type;
    mobile.location = 0.0001f;

    IMobile.MoveParams mp = route.getInfo().getMoveParams()[type];
    mobile.maxSpeed = (float) (mp.speed + Math.random()*mp.vdelta*2-mp.vdelta);
    mobile.maxSpeed = Math.max(1, mobile.maxSpeed);
    mobile.speed = mobile.maxSpeed;
    mobile.maxAccel = mp.acceleration;
    mobile.length = mp.length;
    mobile.route = route;
  }

  private Mobile doCreateMobile(Route route, int type) {
    ISimManager simman = Traffix.simManager();

    Mobile mobile = null;
    if (type == IMobile.Trolley || (type == IMobile.HeavyVehicle && Math.random() < 0.5f)) {
      mobile = new MobileChain(simman);
    } else {
      mobile = new Mobile(simman);
    }
    setupSingleMobile(route, type, mobile);

    if (mobile.route.getNumNodes() < 2) {
      System.out.println("Zonk!");
      return null;
    }

    if (mobile instanceof MobileChain) {
      MobileChain m1 = (MobileChain) mobile;
      MobileChain m2 = new MobileChain(simman);
      m1.nextInChain = m2;
      m2.prevInChain = m1;
      m2.location = mobile.location;
      m2.length = mobile.length;
      m2.type = mobile.type;
      m2.route = mobile.route;
      m2.speed = mobile.maxSpeed;
      m2.maxSpeed = mobile.maxSpeed;
      m2.maxAccel = mobile.maxAccel;
      if (mobile.type == IMobile.HeavyVehicle) {
        m1.length = 6;
        m2.length = 4;
      }
      return m1;//new Mobile[]{m1, m2};
    }

    return mobile;//new Mobile[]{mobile};
  }

  
  public Node getArriveNode() {
    return m_arriveNode;
  }

  public float getArriveTime() {
    return m_arriveTime;
  }

  public RGB getColor() {
    return m_clr;
  }

  public String getName() {
    return m_name;
  }

  public IAccidentPath getPath() {
    return null;
  }

  public Vec2f getPos() {
    return null;
  }

  public APType getType() {
    return m_type;
  }

  public void setArriveNode(Node n) {
    m_arriveNode = n;
  }

  public void setArriveTime(float t) {
    m_arriveTime = t;
  }

  public void setColor(RGB clr) {
    m_clr = clr;
  }

  public void setName(String name) {
    m_name = name;
  }

  public void setPath(IAccidentPath path) {
  }

  public void setType(APType t) {
    m_type = t;
  }
}
