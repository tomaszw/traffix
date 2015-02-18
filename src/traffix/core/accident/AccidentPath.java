/*
 * Created on 2005-09-21
 */

package traffix.core.accident;

public class AccidentPath implements IAccidentPath {

  public int getNumNodes() {
    return 0;
  }

  public float getNodeSpeed(int node) {
    return 0;
  }

  public float getNodePauseTime(int node) {
    return 0;
  }

  public APNode newNode() {
    APNode n = new APNode();
    return n;
  }

}
