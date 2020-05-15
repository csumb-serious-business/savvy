package savvy.core;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.io.fs.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

/**
 * Embeds a Neo4j instance
 * and exposes some basic functionality
 */
public class EmbeddedNeo4j {

  private static final File databaseDirectory = new File("target/savvy-db");
  private static final Label ENTITY_LABEL = Label.label("Entity");
  private static final String NAME = "name";
  private static final String CYPHER_MERGE =
    String.format("MERGE (n:%1$s {%2$s: $%2$s}) RETURN n", ENTITY_LABEL, NAME);

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private GraphDatabaseService _db;
  private DatabaseManagementService _dbms;
  private IndexDefinition _index;


  //=== DB management ===========================================================================\\
  private static void registerShutdownHook(final DatabaseManagementService managementService,
    boolean persist) {

    // registers shutdown hook for Neo4j
    // -> shuts down nicely on JVM exit (even for Ctrl-C)
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (!persist) {
        try {
          FileUtils.deleteRecursively(databaseDirectory);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      managementService.shutdown();
    }));
  }

  /**
   * creates an instance of the database
   *
   * @param persistent whether the database is persistent
   * @throws IOException when the database isn't found (shouldn't happen)
   */
  public void createDb(boolean persistent) throws IOException {
    if (!persistent) {
      FileUtils.deleteRecursively(databaseDirectory);
    }
    _dbms = new DatabaseManagementServiceBuilder(databaseDirectory).build();
    _db = _dbms.database(DEFAULT_DATABASE_NAME);

    // create constraint & index
    createConstraint();

    registerShutdownHook(_dbms, persistent);
  }

  /**
   * create constraint for unique entities (by name)
   * create index for lookup/edit of entities in db
   * (neo4j automatically creates the index with the constraint)
   */
  public void createConstraint() {

    // create index if needed
    try (var tx = _db.beginTx()) {
      tx.schema().getConstraintByName(NAME);
      _index = tx.schema().getIndexByName(NAME);

    } catch (IllegalArgumentException e) {
      try (var tx = _db.beginTx()) {
        // entity (names) must be unique
        tx.schema().constraintFor(ENTITY_LABEL).assertPropertyIsUnique(NAME).withName(NAME)
          .create();
        _index = tx.schema().getIndexByName(NAME);

        tx.commit();

      }
    }
  }

  /**
   * checks whether the index is populated
   *
   * @return true is index population is complete
   */
  public boolean isIndexed() {
    try (var tx = _db.beginTx()) {
      return tx.schema().getIndexPopulationProgress(_index).getCompletedPercentage() == 100F;
    }
  }

  /**
   * manually shutdown the database
   * (usually we rely on the shutdown hook)
   */
  public void shutDown() {
    System.out.println("Shutting down database ...");
    _dbms.shutdown();

  }


  //=== DB crud =================================================================================\\

  //--- facts -----------------------------------------------------------------------------------\\

  /**
   * add a fact to the database
   *
   * @param subject      the subject of the fact
   * @param relationship the relationship between the subject and object
   * @param object       the object of the fact
   */
  public void createFact(String subject, String relationship, String object) {
    createEntity(subject);
    createEntity(object);

    try (var tx = _db.beginTx()) {
      var subNode = tx.findNode(ENTITY_LABEL, NAME, subject);
      var objNode = tx.findNode(ENTITY_LABEL, NAME, object);

      var rel = subNode.createRelationshipTo(objNode, RelTypes.f2_1);
      rel.setProperty(NAME, relationship);

      tx.commit();
    }

  }

  /**
   * remove a fact from the database
   * note: if an entity has no remaining facts, it will be removed
   */
  public void deleteFact(String subject, String relationship, String object) {
    try (var tx = _db.beginTx()) {

      // find & delete the relationship between subject and object
      var subNode = tx.findNode(ENTITY_LABEL, NAME, subject);
      var objNode = tx.findNode(ENTITY_LABEL, NAME, object);
      subNode.getRelationships(Direction.OUTGOING, RelTypes.f2_1).forEach(rel -> {
        if (rel.getEndNode().equals(objNode) && rel.getProperty(NAME).equals(relationship)) {
          rel.delete();
        }
      });

      // delete subject if now unused
      if (!subNode.hasRelationship()) {
        subNode.delete();
      }

      // delete object if now unused
      if (!objNode.hasRelationship()) {
        objNode.delete();
      }

      tx.commit();
    }
  }



  /**
   * Traverse the graph collecting all relationships and entities
   * that are connected to a given entity and return a fact for each
   *
   * @param entityName the name of the entity to lookup
   * @return a set of Facts corresponding to the relationships
   * found in the traversal
   */
  public Set<Fact> readRelatedFacts(String entityName) {

    var set = new TreeSet<Fact>();
    try (var tx = _db.beginTx()) {
      var found = tx.findNodes(ENTITY_LABEL, NAME, entityName).stream().findFirst();
      if (found.isEmpty()) {
        return Set.of();
      }
      var paths = tx.traversalDescription().depthFirst().relationships(RelTypes.f2_1)
        .evaluator(Evaluators.excludeStartPosition()).traverse(found.get());

      for (var path : paths) {
        set.add(pathAsFact(path));
      }

      return set;

    }
  }

  /**
   * Traverse the graph collecting relationships for all entities
   *
   * @return returns a set of Facts corresponding to the relationships
   */
  public Set<Fact> readAllFacts() {
    var set = new TreeSet<Fact>();

    try (var tx = _db.beginTx()) {
      tx.findNodes(ENTITY_LABEL).stream().forEach(n -> {
        var paths = tx.traversalDescription().depthFirst().relationships(RelTypes.f2_1)
          .evaluator(Evaluators.excludeStartPosition()).traverse(n);

        for (var path : paths) {
          set.add(pathAsFact(path));
        }
      });

    }
    return set;
  }

  /**
   * For a given path, gather its subject, relationship and object
   * and create a fact from it
   *
   * @param path the path containing the relationship
   * @return a corresponding Fact
   */
  private Fact pathAsFact(Path path) {
    var rel = path.lastRelationship();
    var r = rel.getProperty(NAME).toString();
    var s = rel.getStartNode().getProperty(NAME).toString();
    var o = rel.getEndNode().getProperty(NAME).toString();
    return new Fact(s, r, o);
  }



  //--- entities --------------------------------------------------------------------------------\\


  /**
   * adds an entity to the database if it did not exist yet
   * note: it seems the fist call to this takes some time to execute
   *
   * @param name the name of the entity to create
   * @return the node holding the new/already-existing entity
   */
  private Node createEntity(String name) {
    try (var tx = _db.beginTx()) {
      Map<String, Object> params = Map.of(NAME, name);
      Node result = tx.execute(CYPHER_MERGE, params).<Node>columnAs("n").next();
      tx.commit();
      return result;
    }
  }

  /**
   * Gathers a set of all entities within the database
   *
   * @return the entities
   */
  public Set<String> readAllEntities() {
    var entities = new TreeSet<String>();
    try (var tx = _db.beginTx()) {
      tx.findNodes(ENTITY_LABEL).stream()
        .forEach(n -> entities.add(n.getProperty(NAME).toString()));
    }

    return entities;
  }

  //--- relationships ---------------------------------------------------------------------------\\

  /**
   * Gathers a set of all the relationships defined within the database
   *
   * @return the relationships
   */
  public Set<String> readAllRelationships() {
    var relationships = new TreeSet<String>();
    readAllFacts().forEach(f -> relationships.add(f.getRelationship()));
    return relationships;
  }

  // describes the types of relationships within the database
  private enum RelTypes implements RelationshipType {
    f2_1 // a fact relationship: 2 entities & 1 relationship
  }
}
