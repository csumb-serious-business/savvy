package savvy.core.relationship;

import java.io.Serializable;
import java.util.Objects;

/**
 * represents a mapping between an outgoing and incoming relationship this is required because some
 * relationships have duplicate names between their counterpart pairs, which is impossible to model
 * with a plain hashmap
 */
public class Correlate implements Serializable {
  public final String outbound;
  public final String inbound;

  public Correlate(String outbound, String inbound) {
    this.outbound = outbound;
    this.inbound = inbound;
  }

  public boolean hasMember(String member) {
    return outbound.equals(member) || inbound.equals(member);
  }

  public boolean isOutbound(String member) {
    return outbound.equals(member);
  }

  public boolean isIncoming(String member) {
    return inbound.equals(member);
  }

  @Override public String toString() {
    return "Correlate{" + "outgoing='" + outbound + '\'' + ", incoming='" + inbound + '\'' + '}';
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Correlate))
      return false;
    Correlate correlate = (Correlate) o;
    return Objects.equals(outbound, correlate.outbound) && Objects
      .equals(inbound, correlate.inbound);
  }

  @Override public int hashCode() {
    return Objects.hash(outbound, inbound);
  }
}
