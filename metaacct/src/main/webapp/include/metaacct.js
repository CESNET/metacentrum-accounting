
/**

   * zobrazi / ukryje filtr

   */
    var SHOW_FILTR = "zobrazit filtr";
    var HIDE_FILTR = "skrýt filtr";    
    var shown = false;
  function handleFiltr(event) {
    var src = Event.element(event);
    if (src.tagName == 'DIV') {
      src = src.parentNode;
    }
    src.parentNode.select('div.form').each(function (form) {
      if (shown) {
        shown = false;
        form.hide();
      } else {
        shown = true;
        form.show();
      }
    });

    src.select('div').each(function (akce) {
      if (shown) {
        akce.update(HIDE_FILTR);
      } else {
        akce.update(SHOW_FILTR);
      }
    });
    Event.stop(event);
  } 



/**
 * ukryje filtry
 */

function initFiltrs() {
  $$('div.filtr').each (function (filtr) {
    filtr.select('div.form').each(function (form) {
      var hide = true;
      $A(form.down('table').getElementsByTagName('input')).each(function (input) {
      	if ($F(input)) {
          if ((input.type == 'text' && $F(input).length > 0) || (input.type == 'checkbox' && $F(input) == 'true')) {
          	hide = false;
          	return;
          }
      	}
      });

      $A(form.down('table').getElementsByTagName('select')).each(function (select) {
      	if ($F(select)) {
          if ($F(select).length > 0 && $F(select) != '-8-') {
          	hide = false;
          	return;
          }
      	}
      });			  			
      
      if (hide) {
      	form.hide();
      	shown = false;
      } else {
      	shown = true;
      }
    });  	

    filtr.select('h3').each(function (nadpis) {
    	if (shown == false) {
    		var akce = new Element('div', { 'class': 'filtr-akce' }).update(SHOW_FILTR);
    	} else {
    		var akce = new Element('div', { 'class': 'filtr-akce' }).update(HIDE_FILTR);
    	}
    	Element.insert(nadpis, {'top' : akce});
    	Event.observe(nadpis, 'click', handleFiltr.bindAsEventListener(this));
    	Event.observe(akce, 'click', handleFiltr.bindAsEventListener(this));
    });
  });
}

if (typeof $ != 'object' && typeof $ != 'function') {
  alert('Please load prototype.js !!!');
} else {
  if(!Prototype.Browser.WebKit) {    
    Event.observe(document, 'dom:loaded', initFiltrs);
  } else {    
    Event.observe(window, 'load', initFiltrs);
  } 

} 
