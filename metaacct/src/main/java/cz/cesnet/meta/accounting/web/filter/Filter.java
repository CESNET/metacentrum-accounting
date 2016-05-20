package cz.cesnet.meta.accounting.web.filter;

import java.util.Map;

public interface Filter {

  Map<String, Object> getSearchCriteria();
  
  void clear();
  
}
