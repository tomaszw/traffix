/*
 * Created on 2004-08-30
 */

package traffix.core.sim;

import org.tw.web.XmlKit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import traffix.Traffix;
import traffix.core.VehicleGroupSet;
import traffix.core.model.IPersistent;
import traffix.core.sim.entities.IMobile;

public class RouteInfo implements IPersistent, Cloneable {
  public boolean               hidden;
  public String                comment            = "";
  public int                   priority;
  public String                name               = "";
  public int[]                 numArrivedVehicles = new int[IMobile.NumTypes];
  public int                   numArrivedVirtualVehicles;
  public int[]                 numPassedVehicles  = new int[IMobile.NumTypes];
  public int                   numPassedVirtualVehicles;
  public float                 summedStopTime     = 0;
  public float                 summedTravelTime   = 0;

  private Databanks.BankData[] m_banks;

  public RouteInfo() {
    initBanks();
  }

  public void assign(RouteInfo info) {
    for (int i = 0; i < m_banks.length; ++i)
      m_banks[i].assign(info.m_banks[i]);
    numArrivedVirtualVehicles = info.numArrivedVirtualVehicles;
    numPassedVirtualVehicles = info.numPassedVirtualVehicles;
    for (int i = 0; i < numPassedVehicles.length; i++) {
      numPassedVehicles[i] = info.numPassedVehicles[i];
    }
    for (int i = 0; i < numArrivedVehicles.length; i++) {
      numArrivedVehicles[i] = info.numArrivedVehicles[i];
    }

    summedStopTime = info.summedStopTime;
    summedTravelTime = info.summedTravelTime;
    priority = info.priority;
    comment = info.comment;
    name = info.name;
    hidden = info.hidden;
  }

  public void clearBankData(int i) {
    m_banks[i] = new Databanks.BankData();
  }

  @Override
  public RouteInfo clone() {
    try {
      RouteInfo res = (RouteInfo) super.clone();
      res.numPassedVehicles = new int[numPassedVehicles.length];
      for (int i = 0; i < numPassedVehicles.length; i++) {
        res.numPassedVehicles[i] = numPassedVehicles[i];
      }
      res.numArrivedVehicles = new int[numArrivedVehicles.length];
      for (int i = 0; i < numArrivedVehicles.length; i++) {
        res.numArrivedVehicles[i] = numArrivedVehicles[i];
      }
      // FIXME clone banks too
      res.m_banks = new Databanks.BankData[m_banks.length];
      for (int i = 0; i < m_banks.length; i++) {
        res.m_banks[i] = m_banks[i].clone();
      }
      return res;
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  public void copyBankData(int from, int to) {
    m_banks[to].assign(m_banks[from]);
  }

  public VehicleGroupSet getControllingGroups() {
    return bank().controllingGps;
  }

  public VehicleGroupSet getLinkedGroups() {
    return bank().linkedGps;
  }

  public Measure getMeasure() {
    return bank().measure;
  }

  public IMobile.MoveParams[] getMoveParams() {
    return bank().moveParams;
  }

  public int getPreferredSec() {
    return bank().preferredSec;
  }

  public String getXmlTagName() {
    return "routeInfo";
  }

  public void resetStatistics() {
    numArrivedVirtualVehicles = 0;
    numPassedVirtualVehicles = 0;
    for (int i = 0; i < numPassedVehicles.length; i++) {
      numPassedVehicles[i] = 0;
      numArrivedVehicles[i] = 0;
    }
    summedStopTime = 0;
    summedTravelTime = 0;
  }

  public void setControllingGroups(VehicleGroupSet controlGroups) {
    bank().controllingGps = controlGroups;
  }

  public void setLinkedGroups(VehicleGroupSet linkedGroups) {
    bank().linkedGps = linkedGroups;
  }

  public void setMeasure(Measure measure) {
    bank().measure = measure;
  }

  public void setMoveParams(IMobile.MoveParams[] moveParams) {
    bank().moveParams = moveParams;
  }

  public void setPreferredSec(int s) {
    bank().preferredSec = s;
  }

  public boolean xmlLoad(Document document, Element root) {
    try {
      if (root.hasAttribute("hidden"))
        hidden = root.getAttribute("hidden").equals("1") ? true:false;
      if (root.hasAttribute("routeName"))
        name = root.getAttribute("routeName");
      if (root.hasAttribute("priority"))
        priority = Integer.parseInt(root.getAttribute("priority"));
      if (root.hasAttribute("comment"))
        comment = root.getAttribute("comment");
      else
        comment = "";

      Element[] bankElems = XmlKit.childElems(root, "bank");
      initBanks();
      for (int i = 0; i < bankElems.length; ++i) {
        Databanks.BankData bank = m_banks[i];
        Element bankElem = bankElems[i];

        bank.measure.nPh = Integer.parseInt(bankElem.getAttribute("nph"));
        bank.measure.hPh = Integer.parseInt(bankElem.getAttribute("hph"));
        if (bankElem.hasAttribute("busph"))
          bank.measure.busPh = Integer.parseInt(bankElem.getAttribute("busph"));
        if (bankElem.hasAttribute("trolleyph"))
          bank.measure.trolleyPh = Integer.parseInt(bankElem.getAttribute("trolleyph"));
        if (bankElem.hasAttribute("prefSec"))
          bank.preferredSec = Integer.parseInt(bankElem.getAttribute("prefSec"));
        if (bankElem.hasAttribute("pedeInterval"))
          bank.measure.pedeInterval = Integer.parseInt(bankElem.getAttribute("pedeInterval"));
        if (bankElem.hasAttribute("cyclistInterval"))
          bank.measure.cyclistInterval = Integer.parseInt(bankElem.getAttribute("cyclistInterval"));
        
        Element[] mpElems = XmlKit.childElems(bankElem, "moveParams");
        for (int j = 0; j < mpElems.length; ++j) {
          bank.moveParams[j].speed = Float.parseFloat(mpElems[j].getAttribute("speed"));
          bank.moveParams[j].acceleration = Float.parseFloat(mpElems[j]
              .getAttribute("acceleration"));
          bank.moveParams[j].length = Float.parseFloat(mpElems[j].getAttribute("length"));
          if (mpElems[j].hasAttribute("vdelta"))
            bank.moveParams[j].vdelta = Float.parseFloat(mpElems[j]
                .getAttribute("vdelta"));
        }

        Element[] groupSets = XmlKit.childElems(bankElem, "groupSet");
        bank.controllingGps.clear();
        if (!bank.controllingGps.xmlLoad(document, groupSets[0]))
          return false;

        if (groupSets.length > 1) {
          bank.linkedGps.clear();
          if (!bank.linkedGps.xmlLoad(document, groupSets[1]))
            return false;
        }

      }
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  public Element xmlSave(Document document) {
    Element root = document.createElement("routeInfo");
    root.setAttribute("priority", Integer.toString(priority));
    root.setAttribute("comment", comment);
    root.setAttribute("routeName", name);
    root.setAttribute("hidden", hidden ? "1":"0");
    
    for (int i = 0; i < m_banks.length; ++i) {
      Databanks.BankData bank = m_banks[i];
      Element bankElem = document.createElement("bank");
      bankElem.setAttribute("nph", Integer.toString(bank.measure.nPh));
      bankElem.setAttribute("hph", Integer.toString(bank.measure.hPh));
      bankElem.setAttribute("busph", Integer.toString(bank.measure.busPh));
      bankElem.setAttribute("trolleyph", Integer.toString(bank.measure.trolleyPh));
      bankElem.setAttribute("prefSec", Integer.toString(bank.preferredSec));
      bankElem.setAttribute("pedeInterval", Integer.toString(bank.measure.pedeInterval));
      bankElem.setAttribute("cyclistInterval", Integer.toString(bank.measure.cyclistInterval));
      
      for (int j = 0; j < bank.moveParams.length; ++j) {
        Element mp = document.createElement("moveParams");
        mp.setAttribute("speed", Float.toString(bank.moveParams[j].speed));
        mp.setAttribute("acceleration", Float.toString(bank.moveParams[j].acceleration));
        mp.setAttribute("length", Float.toString(bank.moveParams[j].length));
        mp.setAttribute("vdelta", Float.toString(bank.moveParams[j].vdelta));
        bankElem.appendChild(mp);
      }
      bankElem.appendChild(bank.controllingGps.xmlSave(document));
      bankElem.appendChild(bank.linkedGps.xmlSave(document));

      root.appendChild(bankElem);

    }
    return root;
  }

  private Databanks.BankData bank() {
    return m_banks[Traffix.simManager().databanks().getActive()];
  }

  private void initBanks() {
    m_banks = new Databanks.BankData[Databanks.MAX_DATABANKS];
    for (int i = 0; i < m_banks.length; i++) {
      m_banks[i] = new Databanks.BankData();
      m_banks[i].controllingGps.assign(Traffix.model().getGroups());
    }
  }
}