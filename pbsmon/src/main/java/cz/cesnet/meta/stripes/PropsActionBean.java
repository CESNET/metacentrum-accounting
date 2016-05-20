package cz.cesnet.meta.stripes;

import cz.cesnet.meta.pbs.Node;
import cz.cesnet.meta.pbs.Pbsky;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

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

    @DefaultHandler
    public Resolution show() {
        nodes = pbsky.getAllNodes();
        propsMap = new HashMap<String, List<Node>>();
        propsGroupMap = new TreeMap<String, List<Node>>();
        for (Node node : nodes) {
            String[] nodeProps = node.getProperties();
            //prihodit stroj do seznamu stroju s urcitou vlastnosti
            for (String nprop : nodeProps) {
                List<Node> l = propsMap.get(nprop);
                if (l == null) propsMap.put(nprop, (l = new ArrayList<Node>()));
                l.add(node);
            }
            //serazit vlastnosti daneho stroje podle abecedy
            // a prihodit do seznamu stroju se stejnymi vlastnostmi
            Arrays.sort(nodeProps);
            String ps = join(nodeProps,":");
            List<Node> l = propsGroupMap.get(ps);
            if (l == null) propsGroupMap.put(ps, (l = new ArrayList<Node>()));
            l.add(node);
        }
        props = new ArrayList<String>(propsMap.keySet());
        Collections.sort(props);
        return new ForwardResolution("/nodes/props.jsp");
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

    public static String join(String[] strings, String separator) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            if (i != 0) sb.append(separator);
            sb.append(strings[i]);
        }
        return sb.toString();
    }

}
