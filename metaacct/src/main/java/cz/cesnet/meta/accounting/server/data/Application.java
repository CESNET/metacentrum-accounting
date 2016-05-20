package cz.cesnet.meta.accounting.server.data;

import java.util.regex.Pattern;

public class Application {

    private Long id;
    private Long order;
    private String name;
    private String regex;
    private Pattern pattern;

    public Application(long id, String name, long order, String regex) {
        super();
        this.id = id;
        this.name = name;
        this.order = order;
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
    }

    public Application() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public Pattern getPattern() {
        return pattern;
    }
}
