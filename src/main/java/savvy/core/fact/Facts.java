package savvy.core.fact;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.db.EmbeddedNeo4j;
import savvy.core.entity.Entities;
import savvy.core.entity.Entity;
import savvy.core.entity.events.EntitiesRead;
import savvy.core.fact.events.DoFactCreate;
import savvy.core.fact.events.DoFactDelete;
import savvy.core.fact.events.DoFactUpdate;
import savvy.core.fact.events.DoFactsRead;
import savvy.core.fact.events.FactCreated;
import savvy.core.fact.events.FactDeleted;
import savvy.core.fact.events.FactUpdated;
import savvy.core.fact.events.FactsRead;
import savvy.core.relationship.Correlate;
import savvy.core.relationship.Relationship;
import savvy.core.relationship.Relationships;
import savvy.core.relationship.events.RelationshipsRead;

import java.util.HashSet;
import java.util.Set;

/**
 * interfaces with the db
 */
public class Facts {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
  private final Set<Fact> _items = new HashSet<>();
  private final Set<Entity> _entities = new HashSet<>();
  private final Set<Relationship> _relationships = new HashSet<>();
  private EmbeddedNeo4j _db;

  public Set<Fact> getItems() {
    return _items;
  }

  /**
   * iniltializes this with a given db
   *
   * @param db the db to use
   */
  public void init(EmbeddedNeo4j db) {
    _db = db;
    EventBus.getDefault().register(this);
  }

  // === event listeners =========================================================================\\

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoFactCreate ev) {

    Entity s;
    // subject exists -> use existent
    var eFound = Entities.getEntitiesWithIdentifier(_entities, ev.subject);
    if (eFound.isEmpty()) {
      s = new Entity(ev.subject, Set.of());
    } else {
      s = eFound.get(0);
    }

    Entity o;
    // object exists -> use existent
    eFound = Entities.getEntitiesWithIdentifier(_entities, ev.object);
    if (eFound.isEmpty()) {
      o = new Entity(ev.object, Set.of());
    } else {
      o = eFound.get(0);
    }

    Relationship r;
    boolean rIsOutbound = true;
    // relationship exists -> use existent
    var rFound = Relationships.getRelationshipsWithForm(_relationships, ev.relationship);
    if (rFound.isEmpty()) {
      var rText = ev.relationship;
      r = new Relationship(rText, Set.of(new Correlate(rText, ("[←" + rText + "]"))));
    } else {
      r = rFound.get(0);
      rIsOutbound = r.hasOutboundCorrelate(ev.relationship);
    }

    Fact fact;
    if (rIsOutbound) {
      fact = new Fact(s, r, o);
    } else {
      fact = new Fact(o, r, s);
    }

    _db.createFact(fact);
    EventBus.getDefault().post(new FactCreated(fact));
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoFactUpdate ev) {
    if (ev.previous.equals(ev.current)) {
      return;
    }

    _db.deleteFact(ev.previous);
    _db.createFact(ev.current);

    EventBus.getDefault().post(new FactUpdated(ev.previous, ev.current));
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void on(DoFactDelete ev) {
    var fact = ev.fact;
    _db.deleteFact(fact);
    EventBus.getDefault().post(new FactDeleted(fact));
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoFactsRead ev) {
    _items.clear();
    if (ev.filter.equals("")) {
      _items.addAll(_db.readAllFacts());
    } else {
      //
      _items.addAll(_db.readRelatedFacts(ev.filter));
    }

    EventBus.getDefault().post(new FactsRead(this));
  }

  // entities read -> update entities autocomplete
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(EntitiesRead ev) {
    _entities.clear();
    _entities.addAll(ev.entities);
  }

  // relationships read -> update relationships autocomplete
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(RelationshipsRead ev) {
    _relationships.clear();
    _relationships.addAll(ev.relationships);
  }
}
