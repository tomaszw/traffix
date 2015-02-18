/*
 * Created on 2004-07-31
 */

package traffix.ui.sim.entities;

import java.util.HashMap;
import java.util.Map;

public class UiEntityType {
  public static final UiEntityType BUSSTOP = new UiEntityType("busstop");
  public static final UiEntityType BARRIER = new UiEntityType("barrier");
  public static final UiEntityType BARRIERDETECTOR = new UiEntityType("barrierdetector");

  public static final UiEntityType GENERATOR = new UiEntityType("generator");
  public static final UiEntityType NODELABEL = new UiEntityType("nodelabel");
  public static final UiEntityType GRAPH = new UiEntityType("graph");
  public static final UiEntityType GRAPH_EDGE = new UiEntityType("graph_edge");
  public static final UiEntityType GRAPH_NODE = new UiEntityType("graph_node");
  public static final UiEntityType PRESENCEDETECTOR = new UiEntityType("presencedetector");
  public static final UiEntityType LIGHT = new UiEntityType("light");
  public static final UiEntityType PATH = new UiEntityType("path");
  public static final UiEntityType PATH_CONTROL_POINT = new UiEntityType("path_control_point");
  public static final UiEntityType PEDESTRIAN_PATH = new UiEntityType("pedestrian_path");
  public static final UiEntityType TRANSITDETECTOR = new UiEntityType("transitdetector");
  public static final UiEntityType VEHICLE = new UiEntityType("vehicle");
  public static final UiEntityType SLOWDOWNDETECTOR = new UiEntityType("slowdowndetector");
  public static final UiEntityType PEDESTRIANDETECTOR = new UiEntityType("pedestriandetector");
  public static final UiEntityType CONDCLEARDETECTOR = new UiEntityType("condcleardetector");
  
  private static Map<String, UiEntityType> m_idents;
  private String m_id;

  public static UiEntityType fromString(String s) {
    return m_idents.get(s);
  }

  private UiEntityType(String id) {
    if (m_idents == null)
      m_idents = new HashMap<String, UiEntityType>();
    if (m_idents.containsKey(id))
      throw new IllegalArgumentException(id);
    m_idents.put(id, this);
    m_id = id;
  }

  public String toString() {
    return m_id;
  }
}