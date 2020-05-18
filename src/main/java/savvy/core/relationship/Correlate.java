package savvy.core.relationship;

/**
 * represents a mapping between an outgoing and incoming relationship
 * this is required because some relationships have duplicate names
 * between their counterpart pairs, which is impossible to model with
 * a plain hashmap
 */
public class Correlate {
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
