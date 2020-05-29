package savvy.core.db;

import org.neo4j.graphdb.RelationshipType;

/** describes the types of relationships within the database */
public enum RelTypes implements RelationshipType {
  f2_1 // a fact relationship: 2 entities & 1 relationship
}
