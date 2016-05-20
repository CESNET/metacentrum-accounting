package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.util.Page;

import java.util.Collection;
import java.util.Map;

public interface UserManager {

    Map<String, Long> saveUsernames(Collection<String> usernames);

    Map<String, Long> loadUsers();

    Page getAllUsersWithPbsRecordNumber(Integer pageNumber, Integer defaultPageSize, Integer pageSize,
                                        String sortColumn, Boolean ascending);

    long getSize();

    String getUserName(long userId);

    Long getUserId(String username);
}
