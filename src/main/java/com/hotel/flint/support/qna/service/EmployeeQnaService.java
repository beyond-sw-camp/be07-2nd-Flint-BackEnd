package com.hotel.flint.support.qna.service;

import com.hotel.flint.common.enumdir.Option;
import com.hotel.flint.support.qna.domain.QnA;
import com.hotel.flint.support.qna.dto.*;
import com.hotel.flint.support.qna.repository.QnaRepository;
import com.hotel.flint.user.employee.domain.Employee;
import com.hotel.flint.user.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeQnaService {

    private final QnaRepository qnaRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeQnaService(QnaRepository qnaRepository, EmployeeRepository employeeRepository) {
        this.qnaRepository = qnaRepository;
        this.employeeRepository = employeeRepository;
    }

    private Employee getAuthenticatedEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("인증 값::" + authentication + "\n 여기가 끝");
        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String email = userDetails.getUsername();
            return employeeRepository.findByEmailAndDelYN(email, Option.N)
                    .orElseThrow(() -> new SecurityException("인증되지 않은 사용자입니다."));
        } else {
            throw new SecurityException("인증되지 않은 사용자입니다.");
        }
    }


    // QnA 답변 작성
    public QnA createAnswer(Long qnaId, CreateAnswerDto dto) {

        // 관리자 권한으로 로그인된 사용자 가져오기
        Employee employee = getAuthenticatedEmployee();

        QnA qna = qnaRepository.findById(qnaId).orElseThrow(() -> new EntityNotFoundException("해당 질문이 없습니다."));
        if (qna.getRespond().equals(Option.N)) {

            qna = dto.toEntity(qna, employee);
            return qnaRepository.save(qna);

        } else {
            throw new IllegalStateException("이미 답변이 존재합니다.");
        }

    }

    // QnA 답변 수정
    public QnA updateAnswer(Long qnaId, UpdateAnswerDto dto) {

        Employee employee = getAuthenticatedEmployee();

        QnA qna = qnaRepository.findById(qnaId).orElseThrow(() -> new EntityNotFoundException("해당 질문이 없습니다."));

        if (qna.getRespond().equals(Option.Y)) {

            qna = dto.toEntity(qna, employee);
            return qnaRepository.save(qna);

        } else {
            throw new IllegalStateException("답변이 존재하지 않습니다.");
        }


    }

    // QnA 답변 삭제
    public QnA deleteAnswer(Long qnaId) {

        QnA qna = qnaRepository.findById(qnaId).orElseThrow(() -> new EntityNotFoundException("해당 질문이 없습니다."));

        if (qna.getRespond().equals(Option.Y)) {

            qna.DeleteQna();
            return qnaRepository.save(qna);

        } else {
            throw new IllegalStateException("답변이 존재하지 않습니다.");
        }

    }

    // QnA 리스트 목록 전체 조회
    public Page<EmployeeQnaListDto> employeeQnaListPage(Pageable pageable){

        Page<QnA> qnaPageList = qnaRepository.findAll(pageable);
        List<EmployeeQnaListDto> employeeQnaListDtos= qnaPageList
                .stream()
                .map(a -> a.ListEntity())
                .collect(Collectors.toList());

        return new PageImpl<>(employeeQnaListDtos, pageable, qnaPageList.getTotalElements());
    }

    // QnA 및 답변 상세 조회
    public EmployeeQnaDetailDto employeeDetailQnA(Long qnaId){

        QnA qna = qnaRepository.findById(qnaId).orElseThrow(() -> new EntityNotFoundException("해당 질문이 없습니다"));
        EmployeeQnaDetailDto employeeQnaDetailDto = qna.DetailEntity();
        return employeeQnaDetailDto;
    }


}
