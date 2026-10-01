package com.manthan.rentloop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReviewsResponse {

    private String targetEmail;
    private Double averageRating;
    private List<ReviewDto> reviews;
}
