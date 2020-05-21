package savvy.core.entity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.db.EmbeddedNeo4j;
import savvy.core.entity.events.DoEntitiesFilter;
import savvy.core.entity.events.DoEntitiesRead;
import savvy.core.entity.events.DoEntityUpdate;
import savvy.core.entity.events.EntitiesFiltered;
import savvy.core.entity.events.EntitiesRead;
import savvy.core.entity.events.EntityUpdated;
import savvy.core.fact.events.FactCreated;
import savvy.core.fact.events.FactDeleted;
import savvy.core.fact.events.FactUpdated;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * models a group of entities
 */
public class Entities {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final Set<Entity> _items;

  private EmbeddedNeo4j _db;

  public Entities() {
    _items = new HashSet<>();
  }

  /**
   * @return a sorted list of identifiers among all entities
   * these include all names and aliases
   */
  private List<String> getIdentifiers() {
    var identifiers = _items.stream().map(Entity::getIdentifiers).flatMap(Set::stream).sorted()
      .collect(Collectors.toSet());
    return identifiers.stream().sorted().collect(Collectors.toList());
  }

  /**
   * initializes this with a given db
   *
   * @param db the db to use
   */
  public void init(EmbeddedNeo4j db) {
    _db = db;

    // register with event bus
    EventBus.getDefault().register(this);

    refresh();
  }

  private void refresh() {
    _items.clear();
    _items.addAll(_db.readAllEntities());

    var result = new ArrayList<>(_items).stream().sorted().collect(Collectors.toList());
    EventBus.getDefault().post(new EntitiesRead(result));
  }

  // todo -- this belongs to Entity, not Entities [MBR]
  private void entityUpdate(Entity previous, Entity current) {
    if (previous.equals(current)) {
      return;
    }
    var pName = previous.getName();
    var cName = current.getName();
    if (!previous.hasSameName(current)) {
      _db.renameEntity(pName, cName);
    }

    _db.createEntity(current);

    EventBus.getDefault().post(new EntityUpdated(previous, current));
  }

  private void entitiesFilter(String filter) {
    List<Entity> entities = _items.stream().filter(i -> i.getName().contains(filter)).sorted()
      .collect(Collectors.toList());
    EventBus.getDefault().post(new EntitiesFiltered(entities));
  }

  //=== event listeners =========================================================================\\
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoEntitiesRead ev) {
    refresh();
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoEntitiesFilter ev) {
    entitiesFilter(ev.filter);
  }

  // update details of one entity (changing its name or aliases)
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(DoEntityUpdate ev) {
    // entity should update itself
    entityUpdate(ev.previous, ev.current);
  }

  // fact create -> addAll
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactCreated ev) {
    refresh();
  }

  // fact update -> update
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactUpdated ev) {
    refresh();
  }

  // fact deleted -> refresh
  @Subscribe(threadMode = ThreadMode.MAIN) public void on(FactDeleted ev) {
    refresh();
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void on(EntityUpdated ev) {
    refresh();
  }

}
