package savvy.core.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

public class SerDe<T> {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public byte[] serialize(Set<T> set) {
    var baos = new ByteArrayOutputStream();

    try {
      var oos = new ObjectOutputStream(baos);
      oos.writeObject(set);
      oos.flush();
    } catch (IOException e) {
      log.error("error: ", e);
    }
    return baos.toByteArray();
  }


  @SuppressWarnings("unchecked") public Set<T> deserialize(byte[] bytes) {
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

  @SuppressWarnings("unchecked") public Set<T> deserialize(Object bytes) {
    return deserialize((byte[]) bytes);
  }

}
