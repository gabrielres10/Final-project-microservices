package com.futurex.services.FutureXCourseApp;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.micrometer.core.instrument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

@RestController
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    private final MeterRegistry meterRegistry;
    private final Tracer tracer;

    private final Counter accessedCounter;
    private final Counter errorCounter;
    private final Counter savedCounter;
    private final Counter deletedCounter;

    private final Timer fetchCoursesTimer;
    private final Timer saveCourseTimer;
    private final Timer deleteCourseTimer;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    public CourseController(MeterRegistry meterRegistry, Tracer tracer) {
        this.meterRegistry = meterRegistry;
        this.tracer = tracer;

        // Contadores
        this.accessedCounter = meterRegistry.counter("course.controller.accessed.count");
        this.savedCounter = meterRegistry.counter("course.controller.saved.count");
        this.deletedCounter = meterRegistry.counter("course.controller.deleted.count");
        this.errorCounter = meterRegistry.counter("course.controller.error.count");

        // Temporizadores
        this.fetchCoursesTimer = meterRegistry.timer("course.controller.fetch.all.timer");
        this.saveCourseTimer = meterRegistry.timer("course.controller.save.timer");
        this.deleteCourseTimer = meterRegistry.timer("course.controller.delete.timer");
    }

    @GetMapping("/courses")
    public List<Course> getCourses() {
        logger.info("Fetching all courses");
        Span span = tracer.spanBuilder("getCourses").startSpan();
        return fetchCoursesTimer.record(() -> {
            try {
                List<Course> courses = courseRepository.findAll();
                accessedCounter.increment();
                logger.info("Fetched {} courses", courses.size());
                return courses;
            } catch (Exception e) {
                errorCounter.increment();
                logger.error("Error fetching courses", e);
                throw e;
            } finally {
                span.end();
            }
        });
    }

    @GetMapping("/")
    public String getCourseAppHome() {
        logger.info("Received request for course app home");
        Span span = tracer.spanBuilder("getCourseAppHome").startSpan();
        try {
            return "Course App Home";
        } finally {
            span.end();
        }
    }

    @GetMapping("/{id}")
    public Course getSpecificCourse(@PathVariable("id") BigInteger id) {
        logger.info("Fetching course with id {}", id);
        Span span = tracer.spanBuilder("getSpecificCourse").startSpan();
        try {
            Course course = courseRepository.getOne(id);
            logger.info("Fetched course: {}", course != null ? course.getCoursename() : "not found");
            return course;
        } catch (Exception e) {
            errorCounter.increment();
            logger.error("Error fetching specific course", e);
            throw e;
        } finally {
            span.end();
        }
    }

    @PostMapping("/courses")
    public void saveCourse(@RequestBody Course course) {
        logger.info("Saving course: {}", course.getCoursename());
        Span span = tracer.spanBuilder("saveCourse").startSpan();
        saveCourseTimer.record(() -> {
            try {
                courseRepository.save(course);
                savedCounter.increment();
                logger.info("Course saved successfully");
            } catch (Exception e) {
                errorCounter.increment();
                logger.error("Error saving course", e);
                throw e;
            } finally {
                span.end();
            }
        });
    }

    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable BigInteger id) {
        logger.info("Deleting course with id {}", id);
        Span span = tracer.spanBuilder("deleteCourse").startSpan();
        deleteCourseTimer.record(() -> {
            try {
                courseRepository.deleteById(id);
                deletedCounter.increment();
                logger.info("Course deleted successfully");
            } catch (Exception e) {
                errorCounter.increment();
                logger.error("Error deleting course", e);
                throw e;
            } finally {
                span.end();
            }
        });
    }
}

