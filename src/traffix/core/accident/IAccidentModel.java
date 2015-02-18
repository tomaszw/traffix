/*
 * Created on 2005-09-21
 */

package traffix.core.accident;

import java.util.List;

import traffix.core.sim.entities.IMobile;
import traffix.core.sim.graph.Graph;
import traffix.core.sim.graph.Node;

public interface IAccidentModel {
  boolean didStopAlready(Node at, IMobile mobile);
  void makeCoffeBreak(Node at, float breakTime, IMobile mobile);
  void bindPath(String partName, IAccidentPath path);
  Node getNode(String name);
  List<String> getNodeNames();
  float getNodeSpeed(Node n);
  float getNodeStopTime(Node n);
  IAccidentParticipant getParticipant(String name);
  Graph graph();
  boolean hasParticipant(String name);
  void initSim();
  IAccidentParticipant newParticipant();
  IAccidentPath newPath();
  boolean participantArrived(String name);
  List<IAccidentParticipant> participants();
  void setMaxTimeStep(float step);
  void setNodeSpeed(Node n, float speed);
  void setNodeStopTime(Node n, float stopTime);
  void setParticipants(List<IAccidentParticipant> parts);
  void simulateTo(float time);
}
