package com.noel.coursesearch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.noel.coursesearch.document.CourseDocument;
import com.noel.coursesearch.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Testcontainers
class CourseSearchIntegrationTest {

    @Container
    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.11.0")
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
    }

    @BeforeEach
    void setUp() {
        // Clear existing data
        IndexCoordinates indexCoordinates = IndexCoordinates.of("courses");
        if (elasticsearchOperations.indexOps(indexCoordinates).exists()) {
            elasticsearchOperations.indexOps(indexCoordinates).delete();
        }
        elasticsearchOperations.indexOps(indexCoordinates).create();

        // Insert test data
        List<CourseDocument> testCourses = List.of(
                new CourseDocument("1", "Java Programming", List.of("java","programming"),"Learn Java programming fundamentals",
                        "Programming", CourseDocument.CourseType.COURSE, "6th-8th", 12, 15, 99.99,
                        OffsetDateTime.now().plusDays(5)),
                new CourseDocument("2", "Art for Kids",List.of("art","kids"), "Creative art sessions for children",
                        "Art", CourseDocument.CourseType.ONE_TIME, "1st-3rd", 6, 9, 25.00,
                        OffsetDateTime.now().plusDays(2)),
                new CourseDocument("3", "Science Club",List.of("science","club"), "Weekly science experiments and discovery",
                        "Science", CourseDocument.CourseType.CLUB, "4th-6th", 9, 12, 15.00,
                        OffsetDateTime.now().plusDays(7))
        );

        courseRepository.saveAll(testCourses);

        // Wait for indexing to complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testSearchWithoutFilters() throws Exception {
        mockMvc.perform(get("/api/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.courses").isArray())
                .andExpect(jsonPath("$.courses.length()").value(3));
    }

    @Test
    void testSearchWithTextQuery() throws Exception {
        mockMvc.perform(get("/api/search?q=Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.courses[0].title").value("Java Programming"));
    }

    @Test
    void testSearchWithCategoryFilter() throws Exception {
        mockMvc.perform(get("/api/search?category=Art"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.courses[0].category").value("Art"));
    }

    @Test
    void testSearchWithAgeFilter() throws Exception {
        mockMvc.perform(get("/api/search?minAge=8&maxAge=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2)); // Art and Science courses
    }

    @Test
    void testSearchWithPriceFilter() throws Exception {
        mockMvc.perform(get("/api/search?minPrice=20&maxPrice=30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.courses[0].price").value(25.00));
    }

    @Test
    void testSearchWithPagination() throws Exception {
        mockMvc.perform(get("/api/search?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.courses.length()").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void testSearchWithPriceSorting() throws Exception {
        mockMvc.perform(get("/api/search?sort=priceAsc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses[0].price").value(15.00))
                .andExpect(jsonPath("$.courses[1].price").value(25.00))
                .andExpect(jsonPath("$.courses[2].price").value(99.99));
    }
}