package com.ariv.photoshare.common.exception;

import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.Instant;

@Provider
public class GlobalExceptionMapper {

    @ServerExceptionMapper
    public RestResponse<ApiError>
    handleUserAlreadyExists(UserAlreadyExistsException ex) {

        return RestResponse.status(
                Response.Status.CONFLICT,
                new ApiError(
                        "USER_ALREADY_EXISTS",
                        ex.getMessage(),
                        java.time.Instant.now()
                )
        );
    }

    @ServerExceptionMapper
    public RestResponse<ApiError>
    handleUnexpected(Throwable ex) {

        return RestResponse.status(
                Response.Status.INTERNAL_SERVER_ERROR,
                new ApiError(
                        "INTERNAL_SERVER_ERROR",
                        "Unexpected error occurred",
                        java.time.Instant.now()
                )
        );
    }

    @ServerExceptionMapper
    public RestResponse<ApiError>
    handleValidation(ValidationException ex) {

        return RestResponse.status(
                Response.Status.BAD_REQUEST,
                new ApiError(
                        "VALIDATION_ERROR",
                        ex.getMessage(),
                        Instant.now()
                )
        );
    }

    @ServerExceptionMapper
    public RestResponse<ApiError>
    handlePersistence(PersistenceException ex) {

        if (ex.getMessage().contains("users_username_key")) {

            return RestResponse.status(
                    Response.Status.CONFLICT,
                    new ApiError(
                            "USER_ALREADY_EXISTS",
                            "Username already exists",
                            Instant.now()
                    )
            );
        }

        return RestResponse.status(
                Response.Status.INTERNAL_SERVER_ERROR,
                new ApiError(
                        "DATABASE_ERROR",
                        "Database operation failed",
                        Instant.now()
                )
        );
    }
}