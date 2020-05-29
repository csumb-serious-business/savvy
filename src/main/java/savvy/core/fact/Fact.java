package savvy.core.fact;

import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.entity.Entity;
import savvy.core.relationship.Relationship;

/** represents a Fact that relates a particular subject entity with an object entity */
public class Fact implements Comparable<Fact> {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public final Entity subject;
  public final Relationship relationship;
  public final Entity object;
  public final Modifier modifier;

  //  public Fact(Entity subject, Relationship relationship, Entity object) {
  //    this(
  //        subject,
  //        relationship,
  //        object,
  //        new Modifier(true, subject.getName(), relationship.getName(), object.getName()));
  //  }

  public Fact(Entity subject, Relationship relationship, Entity object, Modifier modifier) {
    this.subject = subject;
    this.relationship = relationship;
    this.object = object;
    this.modifier = modifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Fact)) return false;
    Fact fact = (Fact) o;
    return subject.equals(fact.subject)
        && relationship.equals(fact.relationship)
        && object.equals(fact.object)
        && modifier.equals(fact.modifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, relationship, object);
  }

  @Override
  public String toString() {
    return subject.getName() + " | " + relationship.getName() + " | " + object.getName();
  }

  public String toStringWithMods() {
    var s =
        !modifier.subject.isBlank()
            ? modifier.subject + " " + subject.getName()
            : subject.getName();
    var r =
        !modifier.relationship.isBlank()
            ? modifier.relationship + " " + relationship.getName()
            : relationship.getName();

    var o =
        !modifier.object.isBlank() ? modifier.object + " " + object.getName() : object.getName();
    return s + " | " + r + " | " + o;
  }

  public Set<Entity> getEntities() {
    return Set.of(subject, object);
  }

  @Override
  public int compareTo(Fact o) {
    return this.toString().compareTo(o.toString());
  }
}
