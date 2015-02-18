/*
 * Created on 2005-09-21
 */

package traffix.core.accident;

import java.util.*;

import org.tw.persistence.xml.annotations.PersistAs;
import org.tw.persistence.xml.annotations.Transient;

import traffix.Traffix;
import traffix.core.sim.ISimManager;
import traffix.core.sim.entities.IMobile;
import traffix.core.sim.graph.Graph;
import traffix.core.sim.graph.Node;

public class AccidentModel implements IAccidentModel {
  @PersistAs("NodeAccidentData")
  Map<Node, NodeAccData>            m_accData  = new HashMap<Node, NodeAccData>();
  @PersistAs("Participants")
  List<IAccidentParticipant>        m_parts    = new ArrayList<IAccidentParticipant>();
  @Transient
  Map<String, IAccidentParticipant> m_partsMap = new HashMap<String, IAccidentParticipant>();
  @Transient
  float                             m_time     = 0;
  @Transient
  Map<Node, Set<IMobile>>           m_didStop  = new HashMap<Node, Set<IMobile>>();

  class NodeAccData {
    float speed;
    float stopTime;
  }

  public List<String> getNodeNames() {
    ArrayList<String> names = new ArrayList<String>();
    ISimManager man = getSimManager();
    Iterator<Node> iter = man.getGraph().getNodeIterator();
    while (iter.hasNext()) {
      Node n = iter.next();
      if (n.getName() != null)
        names.add(n.getName());
    }
    Collections.sort(names);
    return names;
  }
  public void bindPath(String partName, IAccidentPath path) {
    updatePartCache();
    IAccidentParticipant p = getParticipant(partName);
    if (p != null)
      p.setPath(path);
  }

  public Node getNode(String name) {
    ISimManager man = getSimManager();
    Iterator<Node> iter = man.getGraph().getNodeIterator();
    while (iter.hasNext()) {
      Node n = iter.next();
      if (n.getName() != null && n.getName().equals(name))
        return n;
    }
    return null;
  }

  public float getNodeSpeed(Node n) {
    NodeAccData acc = m_accData.get(n);
    return acc != null ? acc.speed : 0;
  }

  public float getNodeStopTime(Node n) {
    NodeAccData acc = m_accData.get(n);
    return acc != null ? acc.stopTime : 0;
  }

  public IAccidentParticipant getParticipant(String name) {
    return m_partsMap.get(name);
  }

  public ISimManager getSimManager() {
    return Traffix.model().getSimManagerFor(this);
  }

  public Graph graph() {
    return null;
  }

  public boolean hasParticipant(String name) {
    return m_partsMap.containsKey(name);
  }

  public void initSim() {
    updatePartCache();
  }

  public IAccidentParticipant newParticipant() {
    IAccidentParticipant p = new AccidentParticipant();
    m_parts.add(p);
    return p;
  }

  public IAccidentPath newPath() {
    return new AccidentPath();
  }

  public boolean participantArrived(String name) {
    IAccidentParticipant p = m_partsMap.get(name);
    if (p != null)
      return p.getArriveTime() <= m_time;
    return false;
  }

  public List<IAccidentParticipant> participants() {
    return m_parts;
  }

  public void setMaxTimeStep(float step) {
  }

  public void setNodeSpeed(Node n, float speed) {
    NodeAccData acc = m_accData.get(n);
    if (acc == null)
      acc = new NodeAccData();
    acc.speed = speed;
    m_accData.put(n, acc);
  }

  public void setNodeStopTime(Node n, float stopTime) {
    NodeAccData acc = m_accData.get(n);
    if (acc == null)
      acc = new NodeAccData();
    acc.stopTime = stopTime;
    m_accData.put(n, acc);
  }

  public void simulateTo(float time) {
    if (time < m_time)
      return;
    m_time = time;
  }

  private void updatePartCache() {
    m_partsMap.clear();
    for (IAccidentParticipant p : m_parts) {
      m_partsMap.put(p.getName(), p);
    }
  }

  public void setParticipants(List<IAccidentParticipant> parts) {
    m_parts = parts;
  }
  public boolean didStopAlready(Node at, IMobile mobile) {
    Set<IMobile> didAtNode = m_didStop.get(at);
    if (didAtNode != null)
      return didAtNode.contains(mobile);
    return false;
  }
  public void makeCoffeBreak(Node at, float breakTime, IMobile mobile) {
    mobile.stopFor(breakTime);
    Set<IMobile> didAtNode = m_didStop.get(at);
    if (didAtNode == null)
      didAtNode = new HashSet<IMobile>();
    didAtNode.add(mobile);
  }
  public String getXmlTagName() {
    return null;
  }

}
