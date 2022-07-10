# hdip-fhir-in

A quick intro to the Fhir terminology:

Fast Healthcare Interoperability Resources (FHIR) is a standard for exchanging healthcare information electronically. FHIR maintains healthcare domain resources representations mainly in json and xml formats.

Hapi-Fhir-Jpa (https://hapifhir.io/hapi-fhir/) is a complete implementation of the HL7 FHIR (http://hl7.org/fhir/) standard for healthcare interoperability in Java. This project provides a java/spring JPA backend with a front end to interact with resources. In the website , it states that the “recommended way to get started with the HAPI FHIR JPA server module is to begin with the starter project”. ( https://github.com/hapifhir/hapi-fhir-jpaserver-starter). Other than this recommendation, I could not find any specific guide on how to do just that in a real world production setting.

My requirements for using the project include the following:

1. Streamline my development/build/release process by using the jpa-starter project as an explicitly declared dependency , rather than clone the project and modify its internal structure. (Ever heard of the 12 factor app?)
2. Extend the project by
   - Use spring framework for all the functionality extensions, using java Configuration
   - Allowing easy addition of interceptors to extend functionality.
   - Demo receiving a resource and then sending a message to a Kafka topic using an interceptor.
3. Package the app in a runnable jar with an embedded web server. (again see https://12factor.net/processes). Running the application this way (as opposed to as a war on a web server) conforms more with microservice architecture. This allows the app to be run as an independent process that can be scaled up or shut down as needed.
## Dependencies
I’ll be using gradle to manage dependencies. 1st step is to declare the dependency on the war in maven repository. Notice the ‘@war’ extension that brings in the whole war without exploding and picking the jars and classes in it.

    dependencies {
        warOnly 'ca.uhn.hapi.fhir:hapi-fhir-jpaserver-starter:4.2.0@war'
    }

Next, we extract the war contents so I can use its classes, lib and resources(js, css, img). There are a few exclusions here and they will have to be added in the project itself.

    tasks.register("explodeHapiFhir",Copy) {
        from zipTree(configurations.warOnly.singleFile)
        into "lib/hapiFhirWar"
      exclude 'WEB-INF/classes/logback.xml' ,
        'WEB-INF/classes/hapi.properties',
        'WEB-INF/classes/ca/uhn/fhir/to/FhirTesterMvcConfig.class',
        'WEB-INF/classes/ca/uhn/fhir/jpa/starter/FhirTesterConfig.class'
        
Then we add the exploded dependencies.

    dependencies {
        implementation files ('lib/hapiFhirWar/WEB-INF/classes')
        implementation fileTree ('lib/hapiFhirWar/WEB-INF/lib')
        warOnly 'ca.uhn.hapi.fhir:hapi-fhir-jpaserver-starter:4.2.0@war'
    }
    
Next, static resources are copied to current project – see example for js below (same is done for css and img directories)

    tasks.register('copyJs',Copy){
      from 'lib/hapiFhirWar/js'
      into 'src/main/resources/js'
    }
    // other similar copy tasks for css, js, thymeleaf templates
    compileJava {
        dependsOn explodeHapiFhir
        dependsOn copyJs
        dependsOn copyCss
        dependsOn copyImg
        dependsOn copyTemplates
    }

Now running ./gradlew build will copy resources to current project.

## Extending
There are 2 servlets /contexts to register. One is hapi-fhir that handles requests to interact with resources, the other is the fhir-tester app that provides a ui to send requests to the 1st servlet.

Hapi-fhir-jpa uses spring web.xml configuration to register servlets. We replace that with java configuration, taking advantage of capabilities added since servlet 3.0.

    public class FhirServletInitializer implements WebApplicationInitializer {
        @Override
        public void onStartup(ServletContext servletContext)
                   throws ServletException {
            ServletRegistration.Dynamic dispatcher = servletContext
                .addServlet("fhirServlet", 
                (Servlet) new HdipRestfulServer());
            dispatcher.setLoadOnStartup(0);
            dispatcher.addMapping("/fhir/*");
        }
    }
Registering the 2nd servlet using spring

      public class ServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer{
 
        @Override
        protected Class<?>[] getRootConfigClasses() {
            return new Class[]{
          FhirServerConfigCommon.class, FhirServerConfigR4.class, HdipConfig.class} ;
        }
 
        @Override
        protected Class<?>[] getServletConfigClasses() {
          return new Class[]{
           FhirTesterConfig.class,HdipMvcConfigurer.class,
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

We use Java Configuration classes to register components such as the mvc configurer, kafka template.Showing only 1 config class below.

    @Configuration
    public class HdipConfig{
 
        @Value("${topic.resourceCreated}")
        private String resourceCreatedTopic;
        @Value("${hdip.kafka.host}")
        private String kafkaHost;
        @Bean
        public ResourceCreatedInterceptor resourceCreatedInterceptor(KafkaTemplate template){
            return new ResourceCreatedInterceptor
              (kafkaTemplate());
        }
 
        @Bean
        public ProducerFactory<Long, IBaseResource> producerFactory() {
            return new DefaultKafkaProducerFactory<>(producerConfigs());
        }
 
        @Bean
        public Map<String, Object> producerConfigs() {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
                      kafkaHost);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                      StringSerializer.class);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                      StringSerializer.class);
            return props;
        }
 
        @Bean
        public KafkaTemplate<Long,IBaseResource> kafkaTemplate() {
          return new KafkaTemplate<Long,IBaseResource>(producerFactory());
        }
        @Bean
        public KafkaAdmin admin() {
            Map<String, Object> configs = new HashMap<>();
            configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
        return new KafkaAdmin(configs);
        }
 
        @Bean
        public NewTopic resourceCreatedTopic() {
            return TopicBuilder.name("resourceCreated")
                    .partitions(1)
                    .replicas(3)
                    .compact()
                    .build();
        }
 
    }
    
We extend the provided JpaRestfulServer so we can add listeners to it as we wish. This is a cleaner way to extend the hapi fhir project , keeping our business logic separate from the parent dependency itself.


    public class HdipRestfulServer extends JpaRestfulServer {
    private static final long serialVersionUID = 1L;
 
    @SuppressWarnings("unchecked")
    @Override
    protected void initialize() throws ServletException {
        super.initialize();
        ApplicationContext context =  (ApplicationContext)
             getServletContext().getAttribute(
          "org.springframework.web.context.WebApplicationContext.ROOT");
        super.registerInterceptor(
           context.getBean(ResourceCreatedInterceptor.class));
    }

The interceptor listens for Patient resource created events and sends a message to a Kafka queue. This can be used by a downstream process which retrieves the resource from the server for further processing. Obviously you will need a running kafka broker.


    @Interceptor
    public class ResourceCreatedInterceptor {
      static final Logger logger = LoggerFactory.getLogger(ResourceCreatedInterceptor.class);
      private KafkaTemplate template;
      public ResourceCreatedInterceptor(KafkaTemplate template){
       this.template = template;
      }
      @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_CREATED)
      public void handleIntercept
         (IBaseResource resource, RequestDetails reqDetails,
          ServletRequestDetails servletReqDetails){
         if(reqDetails.getResourceName().equals("Patient")){
           template.send("resourceCreated",
           ((Patient) resource).getIdentifierFirstRep()
                               .getValue() ,"Patient");
        template.flush();
        logger.info("Resource Created: "+ resource);
    }
    }
    }


Packaging an Executable Jar
We will use the shadow gradle plugin’

    shadowJar {
      zip64=true
      mergeServiceFiles()
      dependencies {
        exclude(dependency
         ('ca.uhn.hapi.fhir:hapi-fhir-jpaserver-starter:4.2.0@war'))
      }
    }
    
Now the project can be assembled with

    ./gradlew shadowJar

And ran with

    java  -Xmx2048m -Xms512m -jar </path/to/jar>.

I suggest running it with sufficient memory.


Now you can navigate to the Patient link, paste a Patient resource and click create. If you have way to view kafka messages (I use kafdrop), you can see the messages queued up in the kafka topic.
