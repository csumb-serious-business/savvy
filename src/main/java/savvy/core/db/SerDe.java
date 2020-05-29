package savvy.core.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class SerDe<T> {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  /**
   * converts a set into a byte array (for serialization)
   *
   * @param set to convert
   * @return byte array
   */
  public byte[] fromSet(Set<T> set) {
    return fromObject(set);
  }

  /**
   * converts an object into a byte array (for serialization)
   *
   * @param o to convert
   * @return byte array
   */
  public byte[] fromObject(Object o) {
    var baos = new ByteArrayOutputStream();

    try {
      var oos = new ObjectOutputStream(baos);
      oos.writeObject(o);
      oos.flush();

    } catch (IOException e) {
      log.error("error: ", e);
    }
    return baos.toByteArray();
  }

  /**
   * converts a byte array to a set (from deserialization)
   *
   * @param bytes to convert
   * @return a set of T
   */
  public Set<T> toSet(byte[] bytes) {
    var bais = new ByteArrayInputStream(bytes);

    try {
      try (var ois = new ObjectInputStream(bais)) {
        return (Set<T>) ois.readObject();
      } catch (ClassNotFoundException e) {
        log.error("class not found -- ", e);
      }

    } catch (IOException e) {
      log.error("IO expception -- ", e);
    }
    return Set.of();
  }

  /**
   * converts a byte array to a T (from deserialization)
   *
   * @param bytes to convert
   * @return a T
   */
  public T toType(byte[] bytes) {
    var bais = new ByteArrayInputStream(bytes);

    try {
      try (var ois = new ObjectInputStream(bais)) {
        return (T) ois.readObject();
      } catch (ClassNotFoundException e) {
        log.error("class not found -- ", e);
      }
    } catch (IOException e) {
      log.error("IO expception -- ", e);
    }
    return null;
  }

  /**
   * convert an object (holding a byte array) into a T
   *
   * @param bytes to convert
   * @return a T
   */
  public T toType(Object bytes) {
    return toType((byte[]) bytes);
  }

  /**
   * converts an object (holding a byte array) into a set
   *
   * @param bytes to convert
   * @return a set of T
   */
  public Set<T> toSet(Object bytes) {
    return toSet((byte[]) bytes);
  }
}
