package com.foodieblog.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    Page<Post> findAllByAuthorId(Long authorId, Pageable pageable);
    Page<Post> findAllByCategory_Id(Long categoryId, Pageable pageable);
    Page<Post> findAllByAuthorIdAndCategory_Id(Long authorId, Long categoryId, Pageable pageable);
    Page<Post> findAllByStatus(PostStatus status, Pageable pageable);
}
