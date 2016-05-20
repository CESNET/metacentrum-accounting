<div class="menu_stats">
  <stripes:link href="/PbsRecords.action?view">
    <stripes:param name="userId">${actionBean.userId}</stripes:param>
    <stripes:param name="periodInDays">1</stripes:param>
      Last 24 hours
  </stripes:link>
  <stripes:link href="/PbsRecords.action?view">
    <stripes:param name="userId">${actionBean.userId}</stripes:param>
    <stripes:param name="periodInDays">7</stripes:param>
      Last week
  </stripes:link>
  <stripes:link href="/PbsRecords.action?view">
    <stripes:param name="userId">${actionBean.userId}</stripes:param>
    <stripes:param name="periodInDays">30</stripes:param>
      Last month
  </stripes:link>  
</div>