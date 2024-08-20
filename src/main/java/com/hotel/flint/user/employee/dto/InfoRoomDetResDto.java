package com.hotel.flint.user.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfoRoomDetResDto {

    private String roomType;
    private LocalDate checkin;
    private LocalDate checkout;
    private int adultCnt;
    private int childCnt;

    private int adultBfCnt; // 성인 조식 신청인원
    private int childBfCnt;
}
