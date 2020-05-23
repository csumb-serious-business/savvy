package savvy.core.relationship;

import java.io.Serializable;
import java.util.Objects;

/**
 * represents a mapping between an outgoing and incoming relationship
 * this is required because some relationships have duplicate names
 * between their counterpart pairs, which is impossible to model with
 * a plain hashmap
 */
public class Correlate implements Serializable {
  public final String outgoing;
  public final String incoming;

  public Correlate(String outgoing, String incoming) {
    this.outgoing = outgoing;
    this.incoming = incoming;
  }

  public boolean hasMember(String member) {
    return outgoing.equals(member) || incoming.equals(member);
  }

  @Override public String toString() {
    return "Correlate{" + "outgoing='" + outgoing + '\'' + ", incoming='" + incoming + '\'' + '}';
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Correlate))
      return false;
    Correlate correlate = (Correlate) o;
    return Objects.equals(outgoing, correlate.outgoing) && Objects
      .equals(incoming, correlate.incoming);
  }

  @Override public int hashCode() {
    return Objects.hash(outgoing, incoming);
  }
}
