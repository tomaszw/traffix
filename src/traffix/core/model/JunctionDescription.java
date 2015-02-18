/*
 * Created on 2005-08-26
 */

package traffix.core.model;

public class JunctionDescription implements Cloneable {
  public String city;
  public String controllerType;
  public String crossingNum;
  public String date;
  public String note;
  public String projectNum;
  public String streets;

  @Override
  public JunctionDescription clone() {
    try {
      return (JunctionDescription) super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }
}