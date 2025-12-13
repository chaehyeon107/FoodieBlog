package com.foodieblog.comment.dto;

import com.foodieblog.comment.Comment;
import com.foodieblog.comment.CommentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentResponse {
    private Long commentId;
    private Long postId;
    private String content;
    private String authorNickname;
    private CommentStatus status;
    private LocalDateTime createdAt;

    public static CommentResponse from(Comment c) {
        return new CommentResponse(
                c.getCommentId(),
                c.getPost().getId(),           // ✅ Post PK = id
                c.getContent(),
                c.getAuthor().getNickname(),
                c.getStatus(),
                c.getCreatedAt()
        );
    }
}
