package savvy.core.fact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

/**
 * represents a Fact that relates a particular subject entity with an object entity
 */
public class Fact implements Comparable<Fact> {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final String subject;
  private final String relationship;
  private final String object;

  public Fact(String subject, String relationship, String object) {
    this.subject = subject;
    this.relationship = relationship;
    this.object = object;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Fact))
      return false;
    Fact fact = (Fact) o;
    return subject.equals(fact.subject) && relationship.equals(fact.relationship) && object
      .equals(fact.object);
  }

  @Override public int hashCode() {
    return Objects.hash(subject, relationship, object);
  }

  @Override public String toString() {
    return subject + " | " + relationship + " | " + object;
  }

  public String getSubject() {
    return subject;
  }

  public String getRelationship() {
    return relationship;
  }

  public String getObject() {
    return object;
  }

  public Set<String> getEntities() {
    return Set.of(subject, object);
  }

  @Override public int compareTo(Fact o) {
    return this.toString().compareTo(o.toString());
  }

}


