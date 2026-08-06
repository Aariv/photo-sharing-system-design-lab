package com.ariv.photoshare.admin.dto;

public record SeedRequest(

        int users,

        int posts,

        int followers,

        int likes,

        int comments

) {
}