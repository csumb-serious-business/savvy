package savvy.core.db;

import org.neo4j.graphdb.Label;

public final class Constants {
  public static final String DB_DIR = "target/savvy-db";
  public static final String NAME = "name";
  public static final String NAME_A = "nameA";
  public static final String NAME_B = "nameB";

  public static final Label ENTITY_LABEL = Label.label("Entity");
  public static final String ALIASES = "aliases";
  public static final String CORRELATES = "correlates";
  public static final String MODIFIER = "modifier";

  public static final String CYPHER_MERGE =
      String.format("MERGE (n:%1$s {%2$s: $%2$s}) RETURN n;", ENTITY_LABEL, NAME);

  public static final String CYPHER_ALL_BETWEEN =
      String.format(
          "MATCH p=(start:%1$s {%2$s: $%3$s})-[r*]-(end:%1$s {%2$s: $%4$s}) RETURN p;",
          ENTITY_LABEL, NAME, NAME_A, NAME_B);
}
