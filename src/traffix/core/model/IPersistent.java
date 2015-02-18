/*
 * Created on 2004-07-04
 */

package traffix.core.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface IPersistent {
  //
  String getXmlTagName();

  // Serialize object to xml element. Return null if error occured.
  Element xmlSave(Document document);

  // Read object from xml data. Return false if error.
  boolean xmlLoad(Document document, Element element);
}
