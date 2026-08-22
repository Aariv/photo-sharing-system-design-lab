package com.ariv.photoshare.search.repository;

import com.ariv.photoshare.search.entity.SearchDocumentEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SearchDocumentRepository
        implements PanacheRepositoryBase<SearchDocumentEntity, UUID> {

    // Equivalent to: SELECT * FROM search_documents WHERE lower(search_text) LIKE lower(:query) ORDER BY created_at DESC LIMIT :limit
    public List<SearchDocumentEntity> search(
            String query,
            int limit) {

        return find(
                """
                lower(searchText)
                like lower(?1)
                order by createdAt desc
                """,
                "%" + query + "%"
        )
        .page(0, limit)
        .list();
    }
}