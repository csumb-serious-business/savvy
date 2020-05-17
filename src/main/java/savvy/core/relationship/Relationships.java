package savvy.core.relationship;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import savvy.core.db.EmbeddedNeo4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Relationships {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  private final Set<Relationship> _items;
  private final Set<String> _names;
  private final Map<String, String> _correlates;

  private EmbeddedNeo4j _db;

  public Relationships() {
    _items = new HashSet<>();
    _names = new HashSet<>();
    _correlates = new HashMap<>();
  }

  public List<String> getNames() {
    return _names.stream().sorted().collect(Collectors.toList());
  }

  private void notifyNamesUpdated() {
    EventBus.getDefault().post(new RelationshipsNamesUpdated(getNames()));
  }


  public void init(EmbeddedNeo4j db) {
    _db = db;
    var relationships = _db.readAllRelationships();

    // todo this is a stop-gap for full-fledged relationships
    _names.addAll(relationships);
    notifyNamesUpdated();
  }

  public void refresh() {
    _items.clear();
    _names.clear();
    _correlates.clear();

    _names.addAll(_db.readAllRelationships());
    notifyNamesUpdated();
  }

  // todo -- stop-gap
  public void add(String name) {
    if (_names.contains(name)) {
      return;
    }
    _names.add(name);
    notifyNamesUpdated();
  }

  // todo -- stop-gap
  public void update(String previous, String current) {
    var changed = false;
    if (_names.contains(previous)) {
      _names.remove(previous);
      changed = true;
    }

    if (!_names.contains(current)) {
      _names.add(current);
      changed = true;
    }

    if (changed) {
      notifyNamesUpdated();
    }

  }
}
