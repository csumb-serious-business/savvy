package savvy.core.relationship;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Relationship implements Comparable<Relationship> {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  // authoritative name for the relationship
  private final String name;

  // pairs of outgoing and incoming mappings between subjects and objects
  // participating in the relationship
  private final Set<Correlate> correlates;

  /**
   * check if this relationship has a given correlate member
   *
   * @param correlate to look for
   * @return true if the member is present
   */
  public boolean hasCorrelate(String correlate) {

    return correlates.stream().anyMatch(c -> c.hasMember(correlate));
  }

  /**
   * check if this relationship has a given outbound correlate member
   *
   * @param correlate to look for
   * @return true if the member is present and outbound
   */
  public boolean hasOutboundCorrelate(String correlate) {
    return correlates.stream().anyMatch(c -> c.isOutbound(correlate));
  }

  /**
   * check if this relationship has a given form (correlate member)
   *
   * @param form to look for
   * @return true if the form is present
   */
  public boolean hasForm(String form) {
    return name.equals(form) || hasCorrelate(form);
  }

  public Relationship(String name, Set<Correlate> correlates) {
    this.name = name;
    this.correlates = correlates;
  }

  /**
   * @return a Set of all forms of this relationship including its authoritative name and all of its
   *     correlates
   */
  public Set<String> allForms() {
    var forms = new HashSet<String>();
    correlates.forEach(c -> forms.addAll(Set.of(c.outbound, c.inbound)));
    forms.add(name);
    return forms;
  }

  public String getName() {
    return name;
  }

  public Set<Correlate> getCorrelates() {
    return correlates;
  }

  @Override
  public String toString() {
    return "Relationship{" + "name='" + name + '\'' + ", correlates=" + correlates + '}';
  }

  @Override
  public int compareTo(Relationship o) {
    return this.toString().compareTo(o.toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Relationship)) return false;
    Relationship that = (Relationship) o;
    return name.equals(that.name) && correlates.equals(that.correlates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, correlates);
  }
}
