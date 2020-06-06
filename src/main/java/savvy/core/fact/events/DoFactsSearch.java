package savvy.core.fact.events;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** fired to initiate searching related Facts from the DB */
public final class DoFactsSearch {
  public final List<String> filters;
  private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

  public DoFactsSearch(List<String> filters) {

    filters = filters.stream().filter(f -> !f.isBlank()).collect(Collectors.toList());
    log.info("searching for: {}", filters);
    this.filters = filters;
  }
}
