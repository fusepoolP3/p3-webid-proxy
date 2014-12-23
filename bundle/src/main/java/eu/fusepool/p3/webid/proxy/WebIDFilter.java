/*
 * Copyright 2014 Bern University of Applied Sciences.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.fusepool.p3.webid.proxy;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

/**
 * A Filter which performs WebID-authentication and adds appropriate headers.
 *
 * @author Pascal Mainini
 */
@Component(service = Filter.class, property = {"pattern=.*"})
public class WebIDFilter implements Filter {

    private LogService log;

//////////////////////////////////////// Interaction with the container
    /**
     * DS-binding for setting the log-service when it becomes available.
     *
     * @param service the log-service to set
     */
    @Reference(
            name = "log.service",
            service = LogService.class,
            unbind = "unsetLogService"
    )
    protected void setLogService(LogService service) {
        this.log = service;
        log.log(LogService.LOG_INFO, "Obtained logservice!");
    }

    /**
     * DS-binding for removing the log-service when it's no longer available.
     *
     * @param service the log-service being removed
     */
    protected void unsetLogService(LogService service) {
        this.log = null;
    }

//////////////////////////////////////// Implementation of Filter
    /**
     * Initialize the filter. Currently does nothing.
     *
     * @param filterConfig Obtained configuration for the filter.
     * @throws ServletException
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log(LogService.LOG_INFO, "Filter initialzed.");
    }

    /**
     * Method from Filter handling the actual filtering.
     *
     * @param request Incoming request
     * @param response Outgoing response
     * @param chain The filter-chain with the other filters
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(!(request instanceof HttpServletRequest)) {
            // FIXME what behavior when we don't get a HTTP-request?
            // or do we anyways always get an HttpServletRequest because the
            // filtered servlet is a HttpServlet?
            chain.doFilter(request, response);
        } else {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            log(LogService.LOG_INFO, "Filtering request: " + httpRequest.getRemoteAddr() + ":" + httpRequest.getRemotePort()
                    + " (" + httpRequest.getHeader("Host") + ") " + httpRequest.getMethod() + " " + httpRequest.getRequestURI());

            chain.doFilter(request, response);
        }
    }

    /**
     * Properly destroy the filter. Currently does nothing.
     */
    @Override
    public void destroy() {
        log(LogService.LOG_INFO, "Filter destroyed.");
    }

//////////////////////////////////////// Helpers
    private void log(int level, String message) {
        if (log != null) {
            log.log(level, message);
        }
    }
}
