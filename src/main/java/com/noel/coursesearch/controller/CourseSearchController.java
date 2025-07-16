package com.noel.coursesearch.controller;

import com.noel.coursesearch.dto.SearchRequest;
import com.noel.coursesearch.dto.SearchResponse;
import com.noel.coursesearch.service.CourseSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CourseSearchController {

    private final CourseSearchService courseSearchService;

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> searchCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(defaultValue = "upcoming") String sort,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Search request - q: {}, minAge: {}, maxAge: {}, category: {}, type: {}, minPrice: {}, maxPrice: {}, startDate: {}, sort: {}, page: {}, size: {}",
                q, minAge, maxAge, category, type, minPrice, maxPrice, startDate, sort, page, size);

        SearchRequest request = new SearchRequest(q, minAge, maxAge, category, type, minPrice, maxPrice, startDate, sort, page, size);
        SearchResponse response = courseSearchService.searchCourses(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Course Search API is running");
    }
}