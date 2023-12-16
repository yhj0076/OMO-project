package com.OmObe.OmO.Review.mapper;

import com.OmObe.OmO.Review.dto.ReviewDto;
import com.OmObe.OmO.Review.entity.Review;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReviewMapper {
    public Review reviewPostDtoToReview(ReviewDto.Post postDto){
        if(postDto == null){
            return null;
        }
        else{
            Review review = new Review();
            review.setContent(postDto.getContent());
            review.setPlaceName(postDto.getPlaceName());
            return review;
        }
    }

    public Review reviewPatchDtoToReview(ReviewDto.Patch patchDto){
        if(patchDto == null){
            return null;
        }
        else{
            Review review = new Review();
            review.setContent(patchDto.getContent());
            return review;
        }
    }

    public ReviewDto.Response reviewToReviewResponseDto(Review review){
        if(review == null){
            return null;
        }
        else{
            long reviewId = review.getReviewId();
            String content = review.getContent();
            LocalDateTime createdTime = review.getCreatedAt();
            String writer = review.getMember().getNickname();
            String placeName = review.getPlaceName();

            ReviewDto.Response response = new ReviewDto.Response(reviewId,content,writer,createdTime,placeName);
            return response;
        }
    }

    public List<ReviewDto.Response> reviewsToReviewResponseDtos(List<Review> reviews){
        if(reviews == null){
            return null;
        } else {
            List<ReviewDto.Response> responses = new ArrayList<>();
            for(Review review:reviews){
                responses.add(this.reviewToReviewResponseDto(review));
            }
            return responses;
        }
    }
}
