package cz.cesnet.meta.accounting.web.apps;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.LocalizableError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import cz.cesnet.meta.accounting.server.data.Application;
import cz.cesnet.meta.accounting.server.service.AppManager;
import cz.cesnet.meta.accounting.web.AccountingWebBase;

public class Edit extends AccountingWebBase {

  @SpringBean
  AppManager appManager;
  
  Long id;
  @ValidateNestedProperties({
    @Validate(field="order", required=true),
    @Validate(field="name", required=true),
    @Validate(field="regex", required=true)
  })
  Application app;
  
  @DefaultHandler
  @DontValidate
  public Resolution view() {    
    app = appManager.getAppById(id);
    return new ForwardResolution("/apps/edit.jsp");
  }
  
  public Resolution save() {
    appManager.saveApp(app);
    return new RedirectResolution("/apps/View.action");
  }
  @DontValidate
  @HandlesEvent(value="back")
  public Resolution back() {
    return new RedirectResolution("/apps/View.action");
  }

  public Application getApp() {
    return app;
  }

  public void setApp(Application app) {
    this.app = app;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @ValidationMethod(on="save")
  public void validateSave(ValidationErrors errors) {
    try {
      Pattern.compile(app.getRegex());
    } catch (PatternSyntaxException e) {
      errors.add("app.regex", new LocalizableError("apps.edit.regex.syntax"));
    }
    Application a = appManager.getAppByOrder(app.getOrder());
    if (a != null && !a.getId().equals(app.getId())) {
      errors.add("app.order", new LocalizableError("apps.edit.order.nonunique"));
    }
  }
  
}
