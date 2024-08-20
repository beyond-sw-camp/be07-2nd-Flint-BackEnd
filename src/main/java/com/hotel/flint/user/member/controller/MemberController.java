package com.hotel.flint.user.member.controller;

import com.hotel.flint.common.auth.JwtAuthFilter;
import com.hotel.flint.common.auth.JwtTokenProvider;
import com.hotel.flint.common.dto.*;
import com.hotel.flint.common.service.MailService;
import com.hotel.flint.user.member.domain.Member;
import com.hotel.flint.user.member.dto.MemberDetResDto;
import com.hotel.flint.user.member.dto.MemberModResDto;
import com.hotel.flint.user.member.dto.MemberSignUpDto;
import com.hotel.flint.user.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.net.URI;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthFilter jwtAuthFilter;

    @Autowired
    MailService mailService;

    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider, JwtAuthFilter jwtAuthFilter) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @GetMapping("/findemail")
    public ResponseEntity<?> findEmail(@RequestBody Map<String, String> request) {
        try {
            String memberEmail = memberService.findEmail(request.get("phoneNumber"));
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "조회에 성공하였습니다.",
                    "회원님의 이메일은 " + memberEmail + "입니다");
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> memberSignUp(@RequestBody MemberSignUpDto dto) {
//        가입 정보를 redis에 임시 저장하므로 아래에서 데이터를 다시 넣는 작업을 안해도 됨.
        mailService.authEmail(dto.getEmail(), dto);
        return new ResponseEntity<>("이메일로 인증 코드를 발송했습니다.", HttpStatus.OK);
    }

    @PostMapping("/signup/verified")
    public ResponseEntity<?> verifyAuthCode(@RequestBody VerifyRequest verifyRequest) {
        boolean isValid = mailService.verifyAuthCode(verifyRequest.getEmail(), verifyRequest.getAuthCode());
        if (isValid) {
            try {
//                Redis에 임시 저장한 SignUpDto 데이터를 통해 회원 가입 진행함.
                MemberSignUpDto memberSignUpDto = mailService.getSignUpData(verifyRequest.getEmail(), MemberSignUpDto.class);
                memberSignUpDto.setBirthday(LocalDate.parse(memberSignUpDto.getBirthday()).toString());
                Member member = memberService.memberSignUp(memberSignUpDto);
                CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED,
                        member.getLastName() + member.getFirstName() + "님, 회원 가입을 축하합니다.", null);
                return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
            } catch (IllegalArgumentException e) {
                CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
                return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("인증 코드가 올바르지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/findpassword")
    public ResponseEntity<?> findPassword(@RequestBody FindPasswordRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
//        로그인 페이지로 리다이렉트
        httpHeaders.setLocation(URI.create("http://localhost:8080/member/login"));
        mailService.sendTempPassword(request.getEmail());
        return new ResponseEntity<>("임시 비밀번호를 이메일로 발송했습니다.", httpHeaders,HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> doLogin(@RequestBody UserLoginDto dto) {
        try {
            Member member = memberService.login(dto);
            String jwtToken = jwtTokenProvider.createMemberToken(member.getEmail(), member.getId());
            Map<String, Object> loginInfo = new HashMap<>();
            loginInfo.put("membertoken", jwtToken);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "환영합니다 " + member.getFirstName() + member.getLastName() + "님!", loginInfo);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/detail")
    public ResponseEntity<?> userDetail() {
        try {
            MemberDetResDto memberDetail = memberService.memberDetail();
            return new ResponseEntity<>(memberDetail, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.NOT_FOUND.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/delete")
    public ResponseEntity<?> userDelete(@RequestBody String password) {
        try {
            memberService.memberDelete(password);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "삭제 완료", null);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.NOT_FOUND.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.NOT_FOUND);
        }
    }

//    멤버 비밀번호 수정하는 기능
    @PutMapping("/modify")
    public ResponseEntity<?> userModify(@RequestBody MemberModResDto dto) {
        try {
            memberService.updatePassword(dto);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "수정 완료", null);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }
}