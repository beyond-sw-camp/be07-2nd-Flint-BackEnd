package com.hotel.flint.room.domain;

import com.hotel.flint.common.enumdir.RoomView;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomInfo roomInfo;


    @Column(nullable = false, unique = true)
    private Integer roomNumber;

    @Enumerated(value = EnumType.STRING)
    private RoomView roomView;

    @Column(nullable = false)
    private Integer maxOccupancy;
    @Column(nullable = false)
    private Integer roomArea;

}
