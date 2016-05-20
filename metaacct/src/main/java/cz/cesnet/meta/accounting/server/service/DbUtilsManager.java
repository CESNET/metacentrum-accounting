package cz.cesnet.meta.accounting.server.service;

public interface DbUtilsManager {

    Long getNextVal(String tableName, String primaryKeyName);
}
