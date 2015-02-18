/*
 * Created on 2004-06-27
 */

package traffix.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import traffix.Traffix;
import traffix.core.model.IPersistent;
import traffix.core.model.Junction;

public class VehicleGroup implements IPersistent, Cloneable {
  private float m_lengthInMeters;
  private float m_acceleration;
  private int m_approachSpeed;
  private int m_evacuationSpeed;
  private String m_name;
  private int m_num;
  private String m_prefix;
  private int m_uniqueID;
  private int m_junctionIndex;

  /* geometry parameters */
  private int m_numTracks = 1;
  private float m_degreeOfFreedom = 2.1f;
  private int m_busCoeff = 0;
  private int m_parkingCoeff = 0;
  private float m_overflowCoeff = 5;

  private Junction m_junction;

  public static VehicleGroup fromIndex(int i) {
    return Traffix.model().getGroupByIndex(i);
  }

  public static VehicleGroup fromUniqueIdent(int id) {
    return Traffix.model().getGroupById(id);
  }

  public VehicleGroup(Junction junction) {
    m_junction = junction;
  }

  @Override
  public VehicleGroup clone() {
    try {
      VehicleGroup cloned = (VehicleGroup) super.clone();
      return cloned;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }

  public float getAcceleration() {
    return m_acceleration;
  }

  public int getApproachSpeed() {
    return m_approachSpeed;
  }

  public int getBusCoeff() {
    return m_busCoeff;
  }

  public float getDegreeOfFreedom() {
    return m_degreeOfFreedom;
  }

  public String getElectricName() {
    if (getJunctionIndex() == 0)
      return getPrefix() + getNum();
    return getJunctionIndex() + "." + getPrefix() + getNum();
  }

  public int getEvacuationSpeed() {
    return m_evacuationSpeed;
  }

  public int getIndex() {
    return m_junction.getVehicleGroups().indexOf(this);
  }

  public Junction getJunction() {
    return m_junction;
  }

  public int getJunctionIndex() {
    return m_junctionIndex;
  }

  public float getLengthInMeters() {
    return m_lengthInMeters;
  }

  public String getName() {
    return m_name;
  }

  public int getNum() {
    return m_num;
  }

  public int getNumTracks() {
    return m_numTracks;
  }

  public float getOverflowCoeff() {
    return m_overflowCoeff;
  }

  public int getParkingCoeff() {
    return m_parkingCoeff;
  }

  public String getPrefix() {
    return m_prefix;
  }

  public int getUniqueId() {
    return getUniqueID();
  }

  public int getUniqueID() {
    return m_uniqueID;
  }

  public String getXmlTagName() {
    return "group";
  }

  public boolean isNormal() {
    return getPrefix().equals("k");
  }

  public void setAcceleration(float acceleration) {
    this.m_acceleration = acceleration;
  }

  public void setApproachSpeed(int approachSpeed) {
    this.m_approachSpeed = approachSpeed;
  }

  public void setBusCoeff(int busCoeff) {
    this.m_busCoeff = busCoeff;
  }

  public void setDegreeOfFreedom(float degreeOfFreedom) {
    this.m_degreeOfFreedom = degreeOfFreedom;
  }

  public void setEvacuationSpeed(int evacuationSpeed) {
    this.m_evacuationSpeed = evacuationSpeed;
  }

  public void setJunction(Junction junction) {
    m_junction = junction;
  }

  public void setJunctionIndex(int junctionIndex) {
    this.m_junctionIndex = junctionIndex;
  }

  public void setLengthInMeters(float lengthInMeters) {
    this.m_lengthInMeters = lengthInMeters;
  }

  public void setName(String name) {
    this.m_name = name;
  }

  public void setNum(int num) {
    this.m_num = num;
  }

  public void setNumTracks(int numTracks) {
    this.m_numTracks = numTracks;
  }

  public void setOverflowCoeff(float overflowCoeff) {
    this.m_overflowCoeff = overflowCoeff;
  }

  public void setParkingCoeff(int parkingCoeff) {
    this.m_parkingCoeff = parkingCoeff;
  }

  public void setPrefix(String prefix) {
    this.m_prefix = prefix;
  }

  public void setUniqueID(int uniqueID) {
    this.m_uniqueID = uniqueID;
  }

  public boolean xmlLoad(Document document, Element root) {
    if (!root.getTagName().equals("group"))
      return false;
    if (!root.hasAttribute("uniqueIdent"))
      return false;
    setUniqueID(Integer.parseInt(root.getAttribute("uniqueIdent")));

    if (!root.hasAttribute("num"))
      return false;
    setNum(Integer.parseInt(root.getAttribute("num")));

    if (!root.hasAttribute("type"))
      return false;
    setPrefix(root.getAttribute("type"));

    if (!root.hasAttribute("name"))
      return false;
    setName(root.getAttribute("name"));

    if (!root.hasAttribute("evacuationSpeed"))
      return false;
    setEvacuationSpeed(Integer.parseInt(root.getAttribute("evacuationSpeed")));

    if (!root.hasAttribute("approachSpeed"))
      return false;
    setApproachSpeed(Integer.parseInt(root.getAttribute("approachSpeed")));

    if (root.hasAttribute("projnum"))
      setJunctionIndex(Integer.parseInt(root.getAttribute("projnum")));

    if (root.hasAttribute("accel"))
      setAcceleration(Float.parseFloat(root.getAttribute("accel")));
    else
      setAcceleration(0);
    if (root.hasAttribute("length"))
      setLengthInMeters(Float.parseFloat(root.getAttribute("length")));
    else
      setLengthInMeters(0);

    if (root.hasAttribute("numTracks"))
      setNumTracks(Integer.parseInt(root.getAttribute("numTracks")));
    if (root.hasAttribute("degreeOfFreedom"))
      setDegreeOfFreedom(Float.parseFloat(root.getAttribute("degreeOfFreedom")));
    if (root.hasAttribute("busCoeff"))
      setBusCoeff(Integer.parseInt(root.getAttribute("busCoeff")));
    if (root.hasAttribute("parkingCoeff"))
      setParkingCoeff(Integer.parseInt(root.getAttribute("parkingCoeff")));
    if (root.hasAttribute("overflowCoeff"))
      setOverflowCoeff(Float.parseFloat(root.getAttribute("overflowCoeff")));

    return true;
  }

  public Element xmlSave(Document document) {
    Element e = document.createElement("group");
    e.setAttribute("num", Integer.toString(getNum()));
    e.setAttribute("type", getPrefix());
    e.setAttribute("name", getName());
    e.setAttribute("evacuationSpeed", Integer.toString(getEvacuationSpeed()));
    e.setAttribute("approachSpeed", Integer.toString(getApproachSpeed()));
    e.setAttribute("uniqueIdent", Integer.toString(getUniqueID()));
    e.setAttribute("accel", Float.toString(getAcceleration()));
    e.setAttribute("length", Float.toString(getLengthInMeters()));
    e.setAttribute("projnum", Integer.toString(getJunctionIndex()));
    e.setAttribute("numTracks", Integer.toString(getNumTracks()));
    e.setAttribute("degreeOfFreedom", Float.toString(getDegreeOfFreedom()));
    e.setAttribute("overflowCoeff", Float.toString(getOverflowCoeff()));
    e.setAttribute("busCoeff", Integer.toString(getBusCoeff()));
    e.setAttribute("parkingCoeff", Integer.toString(getParkingCoeff()));

    return e;
  }
}