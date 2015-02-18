/*
 * Created on 2004-09-02
 */

package traffix.core.sim;

import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import traffix.Traffix;
import traffix.core.VehicleGroupSet;
import traffix.core.model.IPersistent;
import traffix.core.sim.entities.IMobile;
import traffix.core.sim.entities.Mobile;

import java.util.Iterator;
import java.util.List;

public class Databanks implements IPersistent {

  public static final int MAX_DATABANKS = 10;

  private int m_active;
  private String[] m_comments;

  public static class BankData implements Cloneable {
    public VehicleGroupSet controllingGps = new VehicleGroupSet();
    public VehicleGroupSet linkedGps = new VehicleGroupSet();
    public Measure measure = new Measure();
    public IMobile.MoveParams[] moveParams = Mobile.createDefaultMoveParams();
    public int preferredSec = 0;

    public void assign(BankData b) {
      measure.assign(b.measure);
      controllingGps.assign(b.controllingGps);
      linkedGps.assign(b.linkedGps);
      for (int i = 0; i < moveParams.length; ++i)
        moveParams[i].assign(b.moveParams[i]);
      preferredSec = b.preferredSec;
    }
    
    @Override
    public BankData clone() {
      try {
        BankData res = (BankData) super.clone();
        res.controllingGps = controllingGps.clone();
        res.linkedGps = linkedGps.clone();
        res.measure = measure.clone();
        res.moveParams = new IMobile.MoveParams[moveParams.length];
        for (int i = 0; i < res.moveParams.length; i++) {
          res.moveParams[i] = moveParams[i].clone();
        }
        return res;
      } catch (CloneNotSupportedException e) {
        e.printStackTrace();
        return null;
      }
    }


  }

  public Databanks() {
    m_comments = new String[MAX_DATABANKS];
    for (int i = 0; i < m_comments.length; i++) {
      m_comments[i] = "";
    }
  }

  public void clear() {
    List<Route> routes = Traffix.simManager().getRoutes();
    for (Iterator<Route> iter = routes.iterator(); iter.hasNext();) {
      Route route = iter.next();
      RouteInfo info = route.getInfo();
      for (int i = 0; i < MAX_DATABANKS; ++i)
        info.clearBankData(i);
      //route.getBeginningNode().updateRouteInfos();
    }
    for (int i = 0; i < m_comments.length; ++i)
      m_comments[i] = "";
  }

  public void clear(int b) {
    List<Route> routes = Traffix.simManager().getRoutes();
    for (Iterator<Route> iter = routes.iterator(); iter.hasNext();) {
      Route route = iter.next();
      RouteInfo info = route.getInfo();
      info.clearBankData(b);
      //route.getBeginningNode().updateRouteInfos();
    }
    setComment(b, "");
  }

  public void copy(int from, int to) {
    List<Route> routes = Traffix.simManager().getRoutes();
    for (Iterator<Route> iter = routes.iterator(); iter.hasNext();) {
      Route route = iter.next();
      RouteInfo info = route.getInfo();
      info.copyBankData(from, to);
      //route.getBeginningNode().updateRouteInfos();
    }
    setComment(to, getComment(from));
    Traffix.model().setModified(true);
  }

  public int getActive() {
    return m_active;
  }

  public String getComment(int b) {
    return m_comments[b];
  }

  public String getXmlTagName() {
    return "Databanks";
  }

  public void setActive(int b) {
    m_active = b;
  }

  public void setComment(int b, String comm) {
    m_comments[b] = comm;
  }

  public boolean xmlLoad(Document document, Element root) {
    Element[] commElems = XmlKit.childElems(root, "comment");
    for (int i = 0; i < m_comments.length; ++i)
      m_comments[i] = commElems[i].getAttribute("value");
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement(getXmlTagName());
    for (int i = 0; i < m_comments.length; ++i) {
      Element commElem = document.createElement("comment");
      commElem.setAttribute("value", m_comments[i]);
      root.appendChild(commElem);
    }

    return root;
  }
}