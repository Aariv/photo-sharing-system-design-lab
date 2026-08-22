package com.ariv.photoshare.search.service;

import com.ariv.photoshare.post.repository.PostRepository;
import com.ariv.photoshare.search.dto.SearchResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class SearchService {

    @Inject
    PostRepository postRepository;

    public List<SearchResponse> search(
            String query) {

        return postRepository
                .searchByCaption(query, 50)
                .stream()
                .map(post ->
                        new SearchResponse(
                                post.id,
                                post.userId,
                                post.caption,
                                post.imageUrl,
                                post.createdAt
                        ))
                .toList();
    }
}