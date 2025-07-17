package com.noel.coursesearch.service;

import com.noel.coursesearch.document.CourseDocument;
import com.noel.coursesearch.dto.SearchRequest;
import com.noel.coursesearch.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.json.JsonData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public SearchResponse searchCourses(SearchRequest request) {
        NativeQueryBuilder queryBuilder = NativeQuery.builder();

        // Build the boolean query
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        if ((request.getQ() == null || request.getQ().trim().isEmpty()) &&
                request.getMinAge() == null &&
                request.getMaxAge() == null &&
                (request.getCategory() == null || request.getCategory().trim().isEmpty()) &&
                (request.getType() == null || request.getType().trim().isEmpty()) &&
                request.getMinPrice() == null &&
                request.getMaxPrice() == null &&
                request.getStartDate() == null) {
            return new SearchResponse();
        }
        // Full-text search on title and description
        if (request.getQ() != null && !request.getQ().trim().isEmpty()) {
            MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m
                    .query(request.getQ())
                    .fields("title^2.0", "description^1.0"));
            boolQueryBuilder.must(multiMatchQuery._toQuery());
        }

        // Age range filters
        if (request.getMinAge() != null) {
            RangeQuery maxAgeRange = RangeQuery.of(r -> r
                    .field("maxAge")
                    .gte(JsonData.of(request.getMinAge())));
            boolQueryBuilder.filter(maxAgeRange._toQuery());
        }
        if (request.getMaxAge() != null) {
            RangeQuery minAgeRange = RangeQuery.of(r -> r
                    .field("minAge")
                    .lte(JsonData.of(request.getMaxAge())));
            boolQueryBuilder.filter(minAgeRange._toQuery());
        }

        // Category filter
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            TermQuery categoryTerm = TermQuery.of(t -> t
                    .field("category")
                    .value(request.getCategory()));
            boolQueryBuilder.filter(categoryTerm._toQuery());
        }

        // Type filter
        if (request.getType() != null && !request.getType().trim().isEmpty()) {
            TermQuery typeTerm = TermQuery.of(t -> t
                    .field("type")
                    .value(request.getType()));
            boolQueryBuilder.filter(typeTerm._toQuery());
        }

        // Price range filters
        if (request.getMinPrice() != null) {
            RangeQuery minPriceRange = RangeQuery.of(r -> r
                    .field("price")
                    .gte(JsonData.of(request.getMinPrice())));
            boolQueryBuilder.filter(minPriceRange._toQuery());
        }
        if (request.getMaxPrice() != null) {
            RangeQuery maxPriceRange = RangeQuery.of(r -> r
                    .field("price")
                    .lte(JsonData.of(request.getMaxPrice())));
            boolQueryBuilder.filter(maxPriceRange._toQuery());
        }

        // Date filter - only show courses on or after the given date
        if (request.getStartDate() != null) {
            RangeQuery dateRange = RangeQuery.of(r -> r
                    .field("nextSessionDate")
                    .gte(JsonData.of(request.getStartDate().toString())));
            boolQueryBuilder.filter(dateRange._toQuery());
        }

        queryBuilder.withQuery(boolQueryBuilder.build()._toQuery());

        // Sorting
        Sort sort = buildSort(request.getSort());

        // Pagination
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        queryBuilder.withPageable(pageable);

        // Execute search
        Query searchQuery = queryBuilder.build();
        SearchHits<CourseDocument> searchHits = elasticsearchOperations.search(searchQuery, CourseDocument.class);

        // Extract results
        List<CourseDocument> courses = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        long total = searchHits.getTotalHits();
        int totalPages = (int) Math.ceil((double) total / request.getSize());

        return new SearchResponse(total, courses, request.getPage(), request.getSize(), totalPages);
    }

    private Sort buildSort(String sortParam) {
        if (sortParam == null) {
            sortParam = "upcoming";
        }

        return switch (sortParam.toLowerCase()) {
            case "priceasc" -> Sort.by(Sort.Direction.ASC, "price");
            case "pricedesc" -> Sort.by(Sort.Direction.DESC, "price");
            case "upcoming" -> Sort.by(Sort.Direction.ASC, "nextSessionDate");
            default -> Sort.by(Sort.Direction.ASC, "nextSessionDate");
        };
    }
}