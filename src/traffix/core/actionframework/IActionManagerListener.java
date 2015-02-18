/*
 * Created on 2004-07-06
 */

package traffix.core.actionframework;


public interface IActionManagerListener {
  void actionPerformed();

  void undoPerformed();

  void redoPerformed();
}
