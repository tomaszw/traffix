/*
 * Created on 2005-09-25
 */

package traffix.core.accident;

import java.util.*;

public class APTypeNames {
  static Map<APType, String> names = new HashMap<APType, String>();
  static Map<String, APType> types = new HashMap<String, APType>();
  
  static {
    bind("Pieszy", APType.Pedestrian);
    bind("Rowerzysta", APType.Cyclist);
    bind("Motor", APType.Motor);
    bind("Pojazd", APType.Car);
    bind("Pojazd ciê¿ki", APType.BigCar);
    bind("Autobus", APType.Bus);
    bind("Tramwaj", APType.Trolley);
  }
  
  static void bind(String name, APType type) {
    names.put(type, name);
    types.put(name, type);
  }
  
  public static APType getAPType(String name) {
    return types.get(name);
  }
  
  public static String getName(APType type) {
    return names.get(type);
  }
  
  public static String[] getNames() {
    List<String> names = new ArrayList<String>(APTypeNames.names.values());
    Collections.sort(names);
    return (String[]) names.toArray(new String[names.size()]);
  }
}
