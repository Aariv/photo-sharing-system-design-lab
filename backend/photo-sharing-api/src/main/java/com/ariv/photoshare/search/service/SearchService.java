package com.ariv.photoshare.search.service;

import com.ariv.photoshare.post.repository.PostRepository;
import com.ariv.photoshare.search.dto.SearchResponse;
import com.ariv.photoshare.search.repository.SearchDocumentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class SearchService {

    @Inject
    PostRepository postRepository;

    @Inject
    SearchDocumentRepository searchDocumentRepository;

    public List<SearchResponse> search(
            String query) {

        return searchDocumentRepository
                .search(query, 50)
                .stream()
                .map(doc ->
                        new SearchResponse(
                                doc.postId,
                                doc.authorId,
                                doc.caption,
                                null,
                                doc.createdAt
                        ))
                .toList();
    }
}