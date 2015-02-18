/*
 * Created on 2004-08-21
 */

package traffix.ui.sim.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import traffix.core.sim.entities.*;
import traffix.core.sim.graph.Graph;
import traffix.core.sim.graph.Node;

public class UiEntityFactory {
  private static IUiEntityContextProvider s_provider;

  public static BarrierDetectorUi createBarrierDetectorUi(BarrierDetector bd) {
    return new BarrierDetectorUi(s_provider, bd);
  }

  public static BarrierUi createBarrierUi(Barrier b) {
    return new BarrierUi(s_provider, b);
  }

  public static BusStopUi createBusStopUi(BusStop stop) {
    return new BusStopUi(s_provider, stop);
  }

  public static CondClearDetectorUi createCondClearDetectorUi(CondClearDetector d) {
    return new CondClearDetectorUi(s_provider, d);
  }

  public static GraphUi createGraphUi(Graph graph) {
    return new GraphUi(s_provider, graph);
  }

  public static LightUi createLightUi(Light light) {
    return new LightUi(s_provider, light);
  }

  public static NodeLabelUi createNodeLabelUi(Node n, int id) {
    return new NodeLabelUi(s_provider, n, id);
  }

  public static List<IUiEntity> createNodeLabelUis(Graph graph) {
    List<IUiEntity> uis = new ArrayList<IUiEntity>();
    Iterator<Node> it = graph.getNodeIterator();
    int id = 1;
    while (it.hasNext()) {
      Node n = it.next();
      if (n.isBeginningNode()) {
        uis.add(createNodeLabelUi(n, id));
        ++id;
      }
    }
    return uis;
  }

  public static PedestrianDetectorUi createPedestrianDetectorUi(PedestrianDetector pd) {
    return new PedestrianDetectorUi(s_provider, pd);
  }

  public static PedestrianPathUi createPedestrianPathUi(PedestrianPath path) {
    return new PedestrianPathUi(s_provider, path);
  }

  public static PresenceDetectorUi createPresenceDetectorUi(PresenceDetector pd) {
    return new PresenceDetectorUi(s_provider, pd);
  }

  public static SlowdownDetectorUi createSlowdownDetectorUi(SlowdownDetector pd) {
    return new SlowdownDetectorUi(s_provider, pd);
  }

  public static TransitDetectorUi createTransitDetectorUi(TransitDetector td) {
    return new TransitDetectorUi(s_provider, td);
  }

  public static MobileUi createVehicleUi(IMobile vehicle) {
    return new MobileUi(s_provider, vehicle);
  }

  public static void setUiEntityContextProvider(IUiEntityContextProvider provider) {
    s_provider = provider;
  }
}
