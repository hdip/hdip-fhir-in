package com.hdip.in.fhir;

import ca.uhn.fhir.jpa.starter.FhirServerConfigCommon;
import ca.uhn.fhir.jpa.starter.FhirServerConfigR4;
import com.hdip.in.fhir.FhirTesterConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;



	public class ServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer{

		@Override
		protected Class<?>[] getRootConfigClasses() {
			return new Class[]{
					FhirServerConfigCommon.class, FhirServerConfigR4.class, HdipConfig.class} ;
		}

		@Override
		protected Class<?>[] getServletConfigClasses() {
			return new Class[]{
					FhirTesterConfig.class,HdipMvcConfig.class
			};
		}

		@Override
		protected String[] getServletMappings() {
			return new String[]{"/"};
		}
		@Override
		protected String getServletName(){
			return "spring";
		}
	}
