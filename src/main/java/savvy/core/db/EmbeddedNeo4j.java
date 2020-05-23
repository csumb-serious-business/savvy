package savvy.core.db;

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
import savvy.core.entity.Entity;
import savvy.core.fact.Fact;
import savvy.core.relationship.Correlate;
import savvy.core.relationship.Relationship;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
  private static final String ALIASES = "aliases";
  private static final String CORRELATES = "correlates";
  private static final String CYPHER_MERGE =
    String.format("MERGE (n:%1$s {%2$s: $%2$s}) RETURN n", ENTITY_LABEL, NAME);

  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

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
  public void createFact(Entity subject, Relationship relationship, Entity object) {
    createEntity(subject);
    createEntity(object);

    try (var tx = _db.beginTx()) {
      var subNode = tx.findNode(ENTITY_LABEL, NAME, subject.getName());
      var objNode = tx.findNode(ENTITY_LABEL, NAME, object.getName());

      var rel = subNode.createRelationshipTo(objNode, RelTypes.f2_1);
      rel.setProperty(NAME, relationship.getName());
      rel.setProperty(CORRELATES, new SerDe<Correlate>().serialize(relationship.getCorrelates()));

      tx.commit();
    }

  }

  /**
   * add a fact to the database
   *
   * @param fact the fact to add
   */
  public void createFact(Fact fact) {
    createFact(fact.getSubject(), fact.getRelationship(), fact.getObject());
  }

  /**
   * remove a fact from the database
   * note: if an entity has no remaining facts, it will be removed
   */
  public void deleteFact(Entity subject, Relationship relationship, Entity object) {
    try (var tx = _db.beginTx()) {

      // find & delete the relationship between subject and object
      var subNode = tx.findNode(ENTITY_LABEL, NAME, subject.getName());
      var objNode = tx.findNode(ENTITY_LABEL, NAME, object.getName());
      subNode.getRelationships(Direction.OUTGOING, RelTypes.f2_1).forEach(rel -> {
        if (rel.getEndNode().equals(objNode) && rel.getProperty(NAME)
          .equals(relationship.getName())) {
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
   * Delete a fact from the database
   *
   * @param fact the fact to delete
   */
  public void deleteFact(Fact fact) {
    deleteFact(fact.getSubject(), fact.getRelationship(), fact.getObject());
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
    var rc = new SerDe<Correlate>().deserialize(rel.getProperty(CORRELATES));

    var serde = new SerDe<String>();
    var s = rel.getStartNode().getProperty(NAME).toString();
    var sa = serde.deserialize(rel.getStartNode().getProperty(ALIASES));

    var o = rel.getEndNode().getProperty(NAME).toString();
    var oa = serde.deserialize(rel.getEndNode().getProperty(ALIASES));


    return new Fact(new Entity(s, sa), new Relationship(r, rc), new Entity(o, oa));
  }



  //--- entities --------------------------------------------------------------------------------\\

  /**
   * adds an entity to the database if it did not exist yet
   * note: it seems the fist call to this takes some time to execute
   *
   * @param entity the entity to create
   */
  public void createEntity(Entity entity) {
    try (var tx = _db.beginTx()) {
      var params = new HashMap<String, Object>();
      params.put(NAME, entity.getName());
      Node result = tx.execute(CYPHER_MERGE, params).<Node>columnAs("n").next();
      result.setProperty(ALIASES, new SerDe<String>().serialize(entity.getAliases()));
      tx.commit();
      // return result;
    }
  }

  /**
   * update an Entity in the database
   *
   * @param previous the previous version of the Entity
   * @param current  the current version of the Entity
   */
  public void updateEntity(Entity previous, Entity current) {

    // same authoritative name -> merge them otherwise rename
    if (previous.getName().equals(current.getName())) {
      try (var tx = _db.beginTx()) {
        var entity = tx.findNode(ENTITY_LABEL, NAME, previous.getName());

        entity.setProperty(ALIASES, new SerDe<String>().serialize(current.getAliases()));

        tx.commit();
      }
    } else {
      try (var tx = _db.beginTx()) {
        var pNode = tx.findNode(ENTITY_LABEL, NAME, previous.getName());
        pNode.setProperty(NAME, current.getName());
        pNode.setProperty(ALIASES, new SerDe<String>().serialize(current.getAliases()));

        tx.commit();
      }
    }
  }

  /**
   * Gathers a set of all entities within the database
   *
   * @return the entities
   */
  public Set<Entity> readAllEntities() {
    var entities = new TreeSet<Entity>();
    try (var tx = _db.beginTx()) {
      tx.findNodes(ENTITY_LABEL).stream().forEach(n -> {
        var name = n.getProperty(NAME).toString();
        var aliases = new SerDe<String>().deserialize((byte[]) n.getProperty(ALIASES));
        entities.add(new Entity(name, aliases));
        log.info("reading -- name: {}, aliases: {}", name, aliases);
      });
    }

    return entities;
  }

  //--- relationships ---------------------------------------------------------------------------\\

  /**
   * Gathers a set of all relationships whithin the database
   *
   * @return the relationships
   */
  public Set<Relationship> readAllRelationships() {
    var relationships = new HashSet<Relationship>();
    readAllFacts().forEach(f -> relationships.add(f.getRelationship()));

    return relationships;
  }

  /**
   * update a relationship in the database
   *
   * @param previous the previous version of the relationship
   * @param current  the current version of the relationship
   */
  public void updateRelationship(Relationship previous, Relationship current) {

    try (var tx = _db.beginTx()) {
      // all facts
      tx.findNodes(ENTITY_LABEL).stream().forEach(n -> {
        var paths = tx.traversalDescription().depthFirst().relationships(RelTypes.f2_1)
          .evaluator(Evaluators.excludeStartPosition()).traverse(n);

        for (var path : paths) {
          var rel = path.lastRelationship();
          var rName = rel.getProperty(NAME).toString();

          // if matching relationship
          if (rName.equals(previous.getName())) {
            if (!previous.getName().equals(current.getName())) {
              rel.setProperty(NAME, current.getName());
            }
            if (!previous.getCorrelates().equals(current.getCorrelates())) {
              rel
                .setProperty(CORRELATES, new SerDe<Correlate>().serialize(current.getCorrelates()));
            }
          }
        }
      });

      tx.commit();
    }
  }

  // describes the types of relationships within the database
  private enum RelTypes implements RelationshipType {
    f2_1 // a fact relationship: 2 entities & 1 relationship
  }
}
