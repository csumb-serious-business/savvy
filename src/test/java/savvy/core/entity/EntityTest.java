package savvy.core.entity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityTest {
  private static final Set<String> empty_aliases = Set.of();
  private static final String aliasA = "alias A";
  private static final String aliasB = "alias B";
  private static final String neAlias = "non-existant alias";
  private static final Set<String> aliases = Set.of(aliasA, aliasB);
  private static final String name = "name";
  private static final Set<String> identifiers = Set.of(aliasA, aliasB, name);

  private static final Entity other = new Entity(name, aliases);
  private static final Entity different = new Entity(name, empty_aliases);

  private Entity subject;

  @BeforeEach void setUp() {

  }

  @AfterEach void tearDown() {

  }

  @Test void valid() {

    subject = new Entity(name, aliases);


    assertEquals(name, subject.getName());
    assertEquals(aliases, subject.getAliases());
    assertEquals(identifiers, subject.getIdentifiers());

    assertTrue(subject.hasAlias(aliasA));
    assertFalse(subject.hasAlias(neAlias));

    assertTrue(subject.hasIdentifier(name));
    assertTrue(subject.hasIdentifier(aliasA));
    assertFalse(subject.hasIdentifier(neAlias));

    assertEquals(subject, subject);
    assertEquals(other, subject);
    assertNotEquals("", subject);

    assertEquals(0, subject.compareTo(other));
    assertNotEquals(different, subject);

    // tautological
    assertEquals(subject.hashCode(), Objects.hash(name, aliases));

  }
}
