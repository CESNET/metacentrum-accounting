package cz.cesnet.meta.cloud;

import cz.cesnet.meta.TimeStamped;

import java.util.List;
import java.util.Map;

/**
 * OpenNebula state.
 *
 * Class {@link cz.cesnet.meta.cloud.CloudPhysicalHost} represents physical host, it has two important properties:
 * <ul>
 *     <li>{@link CloudPhysicalHost#getName()}</li> contains DNS name or IP address
 *     <li>{@link CloudPhysicalHost#getFqdn()}</li> contains DNS name
 * </ul>
 *
 * Class {@link CloudVirtualHost} represents virtual host, it has
 * <ul>
 *     <li>{@link CloudVirtualHost#getName()} any name, like "MyDebian6" </li>
 *     <li>{@link CloudVirtualHost#getFqdn()} fully qualified DNS name </li>
 *     <li>{@link CloudVirtualHost#getPhysicalHost()} physical host name linking to {@link CloudPhysicalHost#getName()}</li>
 * </ul>
 * @author Martin Kuba makub@ics.muni.cz
 */
public interface Cloud extends TimeStamped {

    List<CloudPhysicalHost> getPhysicalHosts();

    List<CloudVirtualHost> getVirtualHosts();

    Map<String,List<CloudVirtualHost>> getPhysicalHostToVMsMap();

    Map<String, CloudPhysicalHost> getPhysFqdnToPhysicalHostMap();

    Map<String, CloudPhysicalHost> getVmFqdnToPhysicalHostMap();

    int getRefreshTime();
}
