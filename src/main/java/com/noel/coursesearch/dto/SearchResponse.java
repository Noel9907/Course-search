package com.noel.coursesearch.dto;

import com.noel.coursesearch.document.CourseDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private long total;
    private List<CourseDocument> courses;
    private int page;
    private int size;
    private int totalPages;
}