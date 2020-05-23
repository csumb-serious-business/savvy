package savvy.core.relationship;

import java.io.Serializable;

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

  @Override public String toString() {
    return "Correlate{" + "outgoing='" + outgoing + '\'' + ", incoming='" + incoming + '\'' + '}';
  }
}
