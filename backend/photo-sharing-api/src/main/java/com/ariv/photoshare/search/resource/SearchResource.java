package com.ariv.photoshare.search.resource;

import com.ariv.photoshare.search.dto.SearchResponse;
import com.ariv.photoshare.search.service.SearchService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/v1/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

    @Inject
    SearchService searchService;

    @GET
    public List<SearchResponse> search(
            @QueryParam("q")
            String query) {
        return searchService.search(query);
    }
}