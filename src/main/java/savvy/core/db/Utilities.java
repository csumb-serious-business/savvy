package savvy.core.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

public class Utilities {
  private static final Logger log = LoggerFactory.getLogger(Utilities.class.getSimpleName());

  static byte[] serialize(Set<String> set) {
    var baos = new ByteArrayOutputStream();
    byte[] result = null;

    try {
      var oos = new ObjectOutputStream(baos);
      oos.writeObject(set);
      oos.flush();
    } catch (IOException e) {
      log.error("error: ", e);
    }
    return baos.toByteArray();
  }

  static Set<String> deserialize(byte[] bytes) {
    var bais = new ByteArrayInputStream(bytes);


    Set<String> result = null;
    try {

      try (var ois = new ObjectInputStream(bais)) {
        result = ((Set<String>) ois.readObject());
      } catch (ClassNotFoundException e) {
        log.error("class not found -- ", e);
      }

    } catch (IOException e) {
      log.error("IO expception -- ", e);
    }
    return result;
  }

}
