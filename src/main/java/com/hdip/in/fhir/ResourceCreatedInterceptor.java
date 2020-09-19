package com.hdip.in.fhir;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Interceptor
public class ResourceCreatedInterceptor {

        private static final Logger logger = LoggerFactory.getLogger(ResourceCreatedInterceptor.class);
        private KafkaTemplate template;
        public ResourceCreatedInterceptor(KafkaTemplate template){
            this.template = template;
        }
        @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_CREATED)
        public void handlePatientCreated(IBaseResource resource, RequestDetails reqDetails,
                                         ServletRequestDetails servletReqDetails){
            if (reqDetails.getResourceName().equals ("Patient")) {
                template.send("resourceCreated",
                        ((Patient) resource).getIdentifierFirstRep().getValue(), "Patient");
                template.flush();
                logger.info("Resource Created: " + resource);
            }
        }
        public void handlePatientSearch(IBaseResource resource, RequestDetails reqDetails,
                                        ServletRequestDetails servletReqDetails){
            if (reqDetails.getResourceName().equals ("Patient")) {
                template.send("resourceCreated",
                        ((Patient) resource).getIdentifierFirstRep().getValue(), "Patient");
                template.flush();
                logger.info("Resource Created: " + resource);
            }
        }
}
