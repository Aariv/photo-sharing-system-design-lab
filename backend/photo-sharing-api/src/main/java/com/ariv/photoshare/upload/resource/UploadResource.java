package com.ariv.photoshare.upload.resource;

import com.ariv.photoshare.upload.dto.FileResponse;
import com.ariv.photoshare.upload.dto.UploadResponse;
import com.ariv.photoshare.upload.service.FileStorageService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/api/v1/uploads")
public class UploadResource {

    @Inject
    FileStorageService storageService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public UploadResponse upload(
            @RestForm FileUpload file)
            throws Exception {

        return storageService.upload(file);
    }

    @GET
    @Path("/{objectName}")
    public FileResponse getImage(
            @PathParam("objectName")
            String objectName)
            throws Exception {

        String url =
                storageService
                        .generatePresignedUrl(
                                objectName);

        return new FileResponse(url);
    }

}