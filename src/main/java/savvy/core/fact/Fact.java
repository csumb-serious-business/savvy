package savvy.core.fact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;
import savvy.core.relationship.Relationship;

import java.util.Objects;
import java.util.Set;

/**
 * represents a Fact that relates a particular subject entity with an object entity
 */
public class Fact implements Comparable<Fact> {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final Entity subject;
  private final Relationship relationship;
  private final Entity object;

  public Fact(Entity subject, Relationship relationship, Entity object) {
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
    return subject.getName() + " | " + relationship.getName() + " | " + object.getName();
  }

  public Entity getSubject() {
    return subject;
  }

  public Relationship getRelationship() {
    return relationship;
  }

  public Entity getObject() {
    return object;
  }

  public Set<Entity> getEntities() {
    return Set.of(subject, object);
  }

  @Override public int compareTo(Fact o) {
    return this.toString().compareTo(o.toString());
  }

}


