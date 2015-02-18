/*
 * Created on 2004-09-01
 */

package traffix.core.sim.entities;


public interface IMobileChain extends IMobile {
  IMobileChain getPrevInChain();
  IMobileChain getNextInChain();
  boolean isFirst();
}
