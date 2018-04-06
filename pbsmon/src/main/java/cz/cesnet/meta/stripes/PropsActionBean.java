package cz.cesnet.meta.stripes;

import cz.cesnet.meta.pbs.Node;
import cz.cesnet.meta.pbs.Pbsky;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.integration.spring.SpringBean;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: PropsActionBean.java,v 1.2 2009/05/14 07:33:15 makub Exp $
 */
@UrlBinding("/props")
public class PropsActionBean extends BaseActionBean {

    @SpringBean("pbsky")
    protected Pbsky pbsky;

    private List<Node> nodes;
    private Map<String, List<Node>> propsMap;
    private List<String> props;
    private Map<String, List<Node>> propsGroupMap;

    private String property;

    @DefaultHandler
    public Resolution show() {
        prepareData();
        if(property!=null) {
            if(!props.contains(property)) {
                return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND,"property "+property+" not found");
            }
            return new ForwardResolution("/nodes/property_nodes.jsp");
        }
        return new ForwardResolution("/nodes/props.jsp");
    }

    private void prepareData() {
        nodes = pbsky.getAllNodes();
        propsMap = new HashMap<>();
        propsGroupMap = new TreeMap<>();
        for (Node node : nodes) {
            String[] nodeProps = node.getProperties();
            //prihodit stroj do seznamu stroju s urcitou vlastnosti
            for (String nprop : nodeProps) {
                List<Node> l = propsMap.computeIfAbsent(nprop, k -> new ArrayList<>());
                l.add(node);
            }
            //serazit vlastnosti daneho stroje podle abecedy
            // a prihodit do seznamu stroju se stejnymi vlastnostmi
            Arrays.sort(nodeProps);
            String ps = join(nodeProps,":");
            List<Node> l = propsGroupMap.computeIfAbsent(ps, k -> new ArrayList<>());
            l.add(node);
        }
        props = new ArrayList<>(propsMap.keySet());
        Collections.sort(props);
    }



    public List<Node> getNodes() {
        return nodes;
    }

    public Map<String, List<Node>> getPropsMap() {
        return propsMap;
    }

    public List<String> getProps() {
        return props;
    }

    public Map<String, List<Node>> getPropsGroupMap() {
        return propsGroupMap;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }

    public static String join(String[] strings, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (i != 0) sb.append(separator);
            sb.append(strings[i]);
        }
        return sb.toString();
    }

}
