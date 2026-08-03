package com.ariv.photoshare.comment.dto;

import java.util.List;

public record CommentsResponse(
        List<CommentResponse> items
) {
}