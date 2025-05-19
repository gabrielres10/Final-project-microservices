package com.futurex.services.FutureXCourseCatalog;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
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

    // === MÉTRICAS PERSONALIZADAS ===
    private final Counter homeRequestCounter;
    private final Counter catalogRequestCounter;
    private final Counter courseRequestCounter;
    private final Counter errorCounter;

    private final Timer homeRequestTimer;
    private final Timer catalogRequestTimer;
    private final Timer courseRequestTimer;

    public CatalogController(RestTemplate restTemplate, Tracer tracer, MeterRegistry meterRegistry) {
        this.restTemplate = restTemplate;
        this.tracer = tracer;
        this.meterRegistry = meterRegistry;

        // Inicialización de contadores
        this.homeRequestCounter = meterRegistry.counter("catalog.home.count");
        this.catalogRequestCounter = meterRegistry.counter("catalog.catalog.count");
        this.courseRequestCounter = meterRegistry.counter("catalog.course.count");
        this.errorCounter = meterRegistry.counter("catalog.error.count");

        // Inicialización de temporizadores
        this.homeRequestTimer = meterRegistry.timer("catalog.home.timer");
        this.catalogRequestTimer = meterRegistry.timer("catalog.catalog.timer");
        this.courseRequestTimer = meterRegistry.timer("catalog.course.timer");
    }

    @RequestMapping("/")
    public String getCatalogHome() {
        logger.info("Received request for catalog home");
        Span span = tracer.spanBuilder("getCatalogHome").startSpan();
        homeRequestCounter.increment();
        return homeRequestTimer.record(() -> {
            try {
                String courseAppMessage = restTemplate.getForObject(courseServiceUrl, String.class);
                String response = "Welcome to FutureX Course Catalog " + courseAppMessage;
                logger.info("Returning catalog home response");
                return response;
            } catch (Exception e) {
                errorCounter.increment();
                logger.error("Error in getCatalogHome", e);
                throw e;
            } finally {
                span.end();
            }
        });
    }

    @RequestMapping("/catalog")
    public String getCatalog() {
        logger.info("Received request for catalog");
        Span span = tracer.spanBuilder("getCatalog").startSpan();
        catalogRequestCounter.increment();
        return catalogRequestTimer.record(() -> {
            try {
                String courses = restTemplate.getForObject(courseServiceUrl + "/courses", String.class);
                logger.info("Returning catalog list");
                return "Our courses are " + courses;
            } catch (Exception e) {
                errorCounter.increment();
                logger.error("Error in getCatalog", e);
                throw e;
            } finally {
                span.end();
            }
        });
    }

    @RequestMapping("/firstcourse")
    public String getSpecificCourse() {
        logger.info("Received request for first course");
        Span span = tracer.spanBuilder("getSpecificCourse").startSpan();
        courseRequestCounter.increment();
        return courseRequestTimer.record(() -> {
            try {
                Course course = restTemplate.getForObject(courseServiceUrl + "/1", Course.class);
                logger.info("Returning first course: {}", course != null ? course.getCoursename() : "null");
                return "Our first course is " + (course != null ? course.getCoursename() : "not found");
            } catch (Exception e) {
                errorCounter.increment();
                logger.error("Error in getSpecificCourse", e);
                throw e;
            } finally {
                span.end();
            }
        });
    }
}
