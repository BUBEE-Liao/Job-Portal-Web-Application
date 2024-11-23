package com.bubee.jobportal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

// This configuration class will map requests for /photos to serve file from a directory on our file system
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    private static final String UPLOAD_DIR = "photos";

    @Override
    // pass in the upload directory, and it'll also make use of the registry, the resource handler registry
    // What we do : Override the default implementation to set up a custom resource handler
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        exposeDirectory(UPLOAD_DIR, registry);
    }

    private void exposeDirectory(String uploadDir, ResourceHandlerRegistry registry) {
        Path path = Paths.get(uploadDir);
        // converts the uploadDir String  to a Path
        // Maps requests starting with "/photos/**" to a file system location file:<absolute path to photos directory>
        // the ** will match on all sub-directories
        registry.addResourceHandler("/" + uploadDir + "/**").addResourceLocations("file:" + path.toAbsolutePath() + "/");
        // explanation : anything coming in for a given web request for /photos
        // it'll map over to the actual files on our given file system to show or share or expose those given photos
    }
}
