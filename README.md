# Course Search Application

A Spring Boot application that provides a REST API for searching courses stored in Elasticsearch with advanced filtering, pagination, and sorting capabilities.

## Features

- Full-text search on course titles and descriptions
- Multiple filters: age range, category, course type, price range, and session dates
- Flexible sorting options (by date, price ascending/descending)
- Pagination support
- Bulk indexing of sample course data
- RESTful API endpoints

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- Elasticsearch 8.x (will be started via Docker Compose)

## Quick Start

### 1. Start Elasticsearch

run:

```bash
docker-compose up -d
```

Verify Elasticsearch is running:

```bash
curl http://localhost:9200
```

### 2. Build and Run the Application

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080` and automatically:
- Create the `courses` index in Elasticsearch
- Load sample data from `sample-courses.json`
- Index all courses for searching

### 3. Verify the Setup

Check application health:

```bash
curl http://localhost:8080/api/health
```

## API Documentation

### Search Endpoint

**GET** `/api/search`

#### Query Parameters

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `q` | String | Search query for title/description | `q=Java` |
| `minAge` | Integer | Minimum age filter | `minAge=8` |
| `maxAge` | Integer | Maximum age filter | `maxAge=12` |
| `category` | String | Course category filter | `category=Science` |
| `type` | String | Course type filter | `type=COURSE` |
| `minPrice` | Double | Minimum price filter | `minPrice=20.0` |
| `maxPrice` | Double | Maximum price filter | `maxPrice=100.0` |
| `startDate` | DateTime | Show courses on/after this date | `startDate=2025-06-01T00:00:00` |