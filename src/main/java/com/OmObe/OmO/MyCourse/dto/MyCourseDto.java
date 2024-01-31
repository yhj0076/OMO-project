package com.OmObe.OmO.MyCourse.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class MyCourseDto {
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Post{
        private String courseName;
        private List<String> placeName;
        private List<Long> placeId;
        private List<LocalDateTime> time;
    }

    @Getter
    @Setter
    public static class Patch{
        private String courseName;
        private List<String> placeName;
        private List<Long> placeId;
        private List<LocalDateTime> time;
    }

    @Getter
    @AllArgsConstructor
    public static class Response{
        private String courseName;
        private List<ResponseSmall> contents;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
        private String writerName;
    }

    @Getter
    @AllArgsConstructor
    public static class ResponseSmall{
        private String placeName;
        private Long placeId;
        private LocalDateTime time;
    }
}
