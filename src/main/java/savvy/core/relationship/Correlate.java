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

  /**
   * check if this correlate has a given member
   *
   * @param member to look for
   * @return true if the member is present
   */
  public boolean hasMember(String member) {
    return outbound.equals(member) || inbound.equals(member);
  }

  /**
   * check if a member is present and outbound note this will be false if the member is not present
   * so check hasMember first
   *
   * @param member to check
   * @return true if the member is outbound
   */
  public boolean isOutbound(String member) {
    return outbound.equals(member);
  }

  /**
   * check if a member is present and inbound not this will be false if the member is not present so
   * check hasMember first
   *
   * @param member to check
   * @return true if the member is inbound
   */
  public boolean isIncoming(String member) {
    return inbound.equals(member);
  }

  @Override
  public String toString() {
    return "Correlate{" + "outbound='" + outbound + '\'' + ", inbound='" + inbound + '\'' + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Correlate)) return false;
    Correlate correlate = (Correlate) o;
    return Objects.equals(outbound, correlate.outbound)
        && Objects.equals(inbound, correlate.inbound);
  }

  @Override
  public int hashCode() {
    return Objects.hash(outbound, inbound);
  }
}
