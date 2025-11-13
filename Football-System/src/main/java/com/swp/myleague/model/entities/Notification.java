package com.swp.myleague.model.entities;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Notification {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID notificationId;

    private String notificationTitle;
    private String notificationContent;
    private String notificationDateCreated;

    @ManyToOne
    @JoinColumn(name = "userReceived")
    private User user;

}
