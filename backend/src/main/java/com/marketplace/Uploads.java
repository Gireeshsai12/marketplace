package com.marketplace;
import java.nio.file.*;import org.springframework.context.annotation.*;import org.springframework.web.servlet.config.annotation.*;import org.springframework.beans.factory.annotation.Value;
@Configuration class Uploads implements WebMvcConfigurer {final String path;Uploads(@Value("${app.uploads}")String p){path=Path.of(p).toAbsolutePath().normalize().toUri().toString();}public void addResourceHandlers(ResourceHandlerRegistry r){r.addResourceHandler("/uploads/*.png").addResourceLocations(path.endsWith("/")?path:path+"/");}}
