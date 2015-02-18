/*
 * Created on 2005-09-21
 */

package traffix.core.accident;

public interface IAccidentPath {
  public int getNumNodes();
  public float getNodeSpeed(int node);
  public float getNodePauseTime(int node);
  public APNode newNode();
}
