package com.futurex.services.FutureXCourseCatalog;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CatalogController {

    private static final Logger logger = LoggerFactory.getLogger(CatalogController.class);

    @Value("${course.service.url}")
    private String courseServiceUrl;

    private final RestTemplate restTemplate;
    private final Tracer tracer;
    private final MeterRegistry meterRegistry;

    public CatalogController(RestTemplate restTemplate, Tracer tracer, MeterRegistry meterRegistry) {
        this.restTemplate = restTemplate;
        this.tracer = tracer;
        this.meterRegistry = meterRegistry;
    }

    @RequestMapping("/")
    public String getCatalogHome() {
        logger.info("Received request for catalog home");
        Timer.Sample timer = Timer.start(meterRegistry);
        Span span = tracer.spanBuilder("getCatalogHome").startSpan();
        try {
            String courseAppMessage = restTemplate.getForObject(courseServiceUrl, String.class);
            String response = "Welcome to FutureX Course Catalog " + courseAppMessage;
            logger.info("Returning catalog home response");
            return response;
        } catch (Exception e) {
            logger.error("Error in getCatalogHome", e);
            throw e;
        }finally {
            span.end();
            timer.stop(meterRegistry.timer("catalog.home.request"));
        }
    }

    @RequestMapping("/catalog")
    public String getCatalog() {
        Span span = tracer.spanBuilder("getCatalog").startSpan();
        try {
            String courses = restTemplate.getForObject(courseServiceUrl + "/courses", String.class);
            return "Our courses are " + courses;
        } finally {
            span.end();
        }
    }

    @RequestMapping("/firstcourse")
    public String getSpecificCourse() {
        Span span = tracer.spanBuilder("getSpecificCourse").startSpan();
        try {
            Course course = restTemplate.getForObject(courseServiceUrl + "/1", Course.class);
            return "Our first course is " + course.getCoursename();
        } finally {
            span.end();
        }
    }
}