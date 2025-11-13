package com.swp.myleague.model.entities.admin_request;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Request {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID requestId;

    private String requestTitle;

    @Column(columnDefinition = "TEXT")
    private String requestInfor;

    private LocalDateTime requestDateCreate;
    private LocalDateTime requestDateUpdate;
    
    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

}
