package org.example.documentManager;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class DocumentManager {

    private final Map<String, Document> documentStorage = new HashMap<>();

    public Document save(Document document) {
        if (document.getId() == null) {
            document.setId(UUID.randomUUID().toString());
        }
        if (!documentStorage.containsKey(document.getId())) {
            document.setCreated(Instant.now());
        }
        documentStorage.put(document.getId(), document);
        return document;
    }

    public List<Document> search(SearchRequest request) {
        return documentStorage.values().stream()
                .filter(doc -> matchesRequest(doc, request))
                .collect(Collectors.toList());
    }

    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documentStorage.get(id));
    }

    private boolean matchesRequest(Document document, SearchRequest request) {
        if (request.getTitlePrefixes() != null && !request.getTitlePrefixes().isEmpty()) {
            boolean matches = request.getTitlePrefixes().stream()
                    .anyMatch(prefix -> document.getTitle().startsWith(prefix));
            if (!matches) return false;
        }
        if (request.getContainsContents() != null && !request.getContainsContents().isEmpty()) {
            boolean matches = request.getContainsContents().stream()
                    .anyMatch(content -> document.getContent().contains(content));
            if (!matches) return false;
        }
        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            if (!request.getAuthorIds().contains(document.getAuthor().getId())) return false;
        }
        if (request.getCreatedFrom() != null && document.getCreated().isBefore(request.getCreatedFrom())) {
            return false;
        }
        if (request.getCreatedTo() != null && document.getCreated().isAfter(request.getCreatedTo())) {
            return false;
        }
        return true;
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
