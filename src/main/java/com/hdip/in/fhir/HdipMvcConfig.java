package com.hdip.in.fhir;

import ca.uhn.fhir.to.mvc.AnnotationMethodHandlerAdapterConfigurer;
import ca.uhn.fhir.to.util.WebUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

@EnableWebMvc
@ComponentScan(basePackages = {"ca.uhn.fhir.to","com.hdip.in"})
public class HdipMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry theRegistry) {
        WebUtil.webJarAddBoostrap3(theRegistry);
        WebUtil.webJarAddJQuery(theRegistry);
        WebUtil.webJarAddFontAwesome(theRegistry);
        WebUtil.webJarAddJSTZ(theRegistry);
        WebUtil.webJarAddEonasdanBootstrapDatetimepicker(theRegistry);
        WebUtil.webJarAddMomentJS(theRegistry);
        WebUtil.webJarAddSelect2(theRegistry);
        WebUtil.webJarAddAwesomeCheckbox(theRegistry);
        theRegistry.setOrder(Integer.MAX_VALUE);
        theRegistry.addResourceHandler(new String[]{"/css/**"}).addResourceLocations(new String[]{"classpath:css/"});
        theRegistry.addResourceHandler(new String[]{"/fa/**"}).addResourceLocations(new String[]{"classpath:fa/"});
        theRegistry.addResourceHandler(new String[]{"/fonts/**"}).addResourceLocations(new String[]{"classpath:fonts/"});
        theRegistry.addResourceHandler(new String[]{"/img/**"}).addResourceLocations(new String[]{"classpath:img/"});
        theRegistry.addResourceHandler(new String[]{"/js/**"}).addResourceLocations(new String[]{"classpath:js/"});
    }

    @Bean
    public AnnotationMethodHandlerAdapterConfigurer annotationMethodHandlerAdapterConfigurer() {
        return new AnnotationMethodHandlerAdapterConfigurer();
    }

    @Bean
    public ThymeleafViewResolver viewResolver(SpringTemplateEngine templateEngine) {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine);
        viewResolver.setCharacterEncoding("UTF-8");
        return viewResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine(ITemplateResolver viewResolver) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(viewResolver);
        return templateEngine;
    }
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }
}
