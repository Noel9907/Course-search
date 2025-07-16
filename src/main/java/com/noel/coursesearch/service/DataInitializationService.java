package com.noel.coursesearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.noel.coursesearch.document.CourseDocument;
import com.noel.coursesearch.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        try {
            // Check if index exists and has data
            IndexCoordinates indexCoordinates = IndexCoordinates.of("courses");
            if (elasticsearchOperations.indexOps(indexCoordinates).exists()) {
                long count = courseRepository.count();
                if (count > 0) {
                    log.info("Courses index already exists with {} documents. Skipping data initialization.", count);
                    return;
                }
            }

            // Create index if it doesn't exist
            if (!elasticsearchOperations.indexOps(indexCoordinates).exists()) {
                elasticsearchOperations.indexOps(indexCoordinates).create();
                log.info("Created courses index");
            }

            // Load and index sample data
            loadSampleData();

        } catch (Exception e) {
            log.error("Error during data initialization", e);
        }
    }

    private void loadSampleData() throws IOException {
        ClassPathResource resource = new ClassPathResource("sample-courses.json");

        if (!resource.exists()) {
            log.warn("sample-courses.json not found in resources. Skipping data initialization.");
            return;
        }

        // Configure ObjectMapper for LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());

        // Read the JSON file
        CourseDocument[] courses = objectMapper.readValue(resource.getInputStream(), CourseDocument[].class);

        // Bulk index all courses
        List<CourseDocument> courseList = Arrays.asList(courses);
        courseRepository.saveAll(courseList);

        log.info("Successfully indexed {} courses", courseList.size());
    }
}