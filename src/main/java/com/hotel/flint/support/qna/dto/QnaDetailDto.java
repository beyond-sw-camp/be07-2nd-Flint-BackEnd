package com.hotel.flint.support.qna.dto;

import com.hotel.flint.common.enumdir.Service;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QnaDetailDto {

    private Service service;
    private String title;
    private String contents;
    private LocalDateTime writeTime;
    private String memberEmail; // 작성자

}
