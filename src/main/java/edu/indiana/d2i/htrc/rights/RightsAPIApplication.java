package edu.indiana.d2i.htrc.rights;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.ServletConfig;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

@ApplicationPath("/")
public class RightsAPIApplication extends Application {
	@Context private ServletConfig servletConfig;
	
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(RequestResource.class);
        return s;
    }
    
    @PostConstruct
    private void init() {
    	Hub.init(servletConfig);
    }
}
