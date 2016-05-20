package cz.cesnet.meta.accounting.displaytag;


import org.apache.log4j.Logger;

import javax.servlet.*;
import java.io.IOException;

public class PaginationFilter implements Filter {
    private final Logger logger = Logger.getLogger(PaginationFilter.class);

    public static final Integer DEFAULT_PAGE_SIZE_SHORT = 20;
    public static final Integer DEFAULT_PAGE_SIZE_LONG = 100;
    public static final String PAGE_NUMBER = "page";
    public static final String PAGE_SIZE = "pageSize";
    public static final String SORTING_COLUMN = "sortColumn";
    public static final String ASCENDING = "ascending";

    private FilterConfig filterConfig = null;

    public void destroy() {

    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        Integer pageNumber = 1;
        Integer pageSize = null;
        String sortColumn;
        Boolean ascending = true;

        //page number
        try {

            String ppage = request.getParameter("page");
            if (ppage != null) {
                pageNumber = Integer.parseInt(ppage);
            }
        } catch (NumberFormatException e) {
            logger.warn("cannot parse page", e);
        }
        try {
            String ppageSize = request.getParameter("pageSize");
            if (ppageSize != null) {
                pageSize = Integer.parseInt(ppageSize);
            }
        } catch (NumberFormatException e) {
            logger.warn("cannot parse pageSize", e);
        }

        sortColumn = request.getParameter("sort");

        String dirParam = request.getParameter("dir");
        if (dirParam != null) {
            ascending = "asc".equals(dirParam);
        }

        request.setAttribute(PAGE_NUMBER, pageNumber);
        request.setAttribute(PAGE_SIZE, pageSize);
        request.setAttribute(SORTING_COLUMN, sortColumn);
        request.setAttribute(ASCENDING, ascending);

        logger.debug("page_number: " + pageNumber + ", page_size: " + pageSize + ", sorting_column: " + sortColumn + ", ascending: " + ascending);
        chain.doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

}
