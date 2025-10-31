package com.safetrip.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_file_id")
    private Long userFileId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "file_id", nullable = false)
    private Long fileId;
}