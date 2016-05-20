package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.data.Application;

import java.util.List;


public interface AppManager {

    List<Application> getAllApps();

    void deleteApp(Long id);

    Application getAppById(Long id);

    Application getAppByOrder(Long order);

    void saveApp(Application app);
}
