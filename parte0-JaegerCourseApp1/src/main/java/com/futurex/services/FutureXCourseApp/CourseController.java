package com.futurex.services.FutureXCourseApp;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.List;

@RestController
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
    private final MeterRegistry meterRegistry;

    @Autowired
    private CourseRepository courseRepository;

    public CourseController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        }

    @RequestMapping("/courses")
    public List<Course> getCourses() {
        logger.info("Fetching all courses");
        meterRegistry.counter("courses.accessed").increment();
        List<Course> courses = courseRepository.findAll();
        logger.info("Fetched {} courses", courses.size());
        return courses;
    }

    
/* 
    @RequestMapping("/")
    public String getCourseAppHome() {
        Span span = tracer.spanBuilder("getCourceAppHome").startSpan();
        try {
            return ("Course App Home");
        } finally {
            span.end();
        }
    }

    @RequestMapping("/{id}")
    public Course getSpecificCourse(@PathVariable("id") BigInteger id ) {
        return courseRepository.getOne(id);
    }

    @RequestMapping(method = RequestMethod.POST, value="/courses")
    public void saveCourse(@RequestBody Course course) {
        courseRepository.save(course);
    }

    @RequestMapping(method = RequestMethod.DELETE,value = "{id}")
    public void deleteCourse(@PathVariable BigInteger id) {
        courseRepository.deleteById(id);
    }
*/

}
