package com.myapp.backend.infrastructure.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerServerConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${swagger.port:8081}")
    private int swaggerPort;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setPort(swaggerPort);
        factory.addAdditionalTomcatConnectors(connector);
    }
}
