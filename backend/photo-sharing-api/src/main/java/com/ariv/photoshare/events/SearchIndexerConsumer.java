package com.ariv.photoshare.events;

import com.ariv.photoshare.post.entity.PostEntity;
import com.ariv.photoshare.post.repository.PostRepository;
import com.ariv.photoshare.search.entity.SearchDocumentEntity;
import com.ariv.photoshare.search.repository.SearchDocumentRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class SearchIndexerConsumer {

    @Inject
    PostRepository postRepository;

    @Inject
    SearchDocumentRepository searchRepository;

    @Incoming("post-created-in")
    @Transactional
    public void consume(PostCreatedEvent event) {

        PostEntity post =
                postRepository.findById(
                        event.postId());

        if (post == null) {
            return;
        }

        SearchDocumentEntity document =
                new SearchDocumentEntity();

        document.postId = post.id;
        document.authorId = post.userId;
        document.caption = post.caption;

        document.searchText =
                post.caption.toLowerCase();

        document.createdAt =
                post.createdAt;

        searchRepository.persist(document);
    }
}