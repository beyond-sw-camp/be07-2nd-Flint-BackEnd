package com.hotel.flint.user.employee.service;

import com.hotel.flint.common.enumdir.Department;
import com.hotel.flint.common.enumdir.Option;
import com.hotel.flint.room.domain.RoomInfo;
import com.hotel.flint.reserve.room.domain.RoomReservation;
import com.hotel.flint.room.repository.RoomDetailsRepository;
import com.hotel.flint.room.repository.RoomInfoRepository;
import com.hotel.flint.room.repository.RoomPriceRepository;
import com.hotel.flint.reserve.room.repository.RoomReservationRepository;
import com.hotel.flint.user.employee.domain.Employee;
import com.hotel.flint.user.employee.dto.InfoRoomResDto;
import com.hotel.flint.user.employee.dto.InfoUserResDto;
import com.hotel.flint.user.employee.repository.EmployeeRepository;
import com.hotel.flint.user.member.domain.Member;
import com.hotel.flint.user.member.repository.MemberRepository;
import com.hotel.flint.user.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@Transactional
public class EmployeeRoomService {
    private final RoomPriceRepository roomPriceRepository;
    private final RoomDetailsRepository roomDetailsRepository;
    private final RoomInfoRepository roomInfoRepository;
    private final RoomReservationRepository roomReservationRepository;
    private final EmployeeService employeeService;
    private final MemberRepository memberRepository;
    private final EmployeeRepository employeeRepository;
    private final MemberService memberService;

    @Autowired
    public EmployeeRoomService(RoomPriceRepository roomPriceRepository,
                               RoomDetailsRepository roomDetailsRepository,
                               RoomInfoRepository roomInfoRepository,
                               RoomReservationRepository roomReservationRepository,
                               EmployeeService employeeService,
                               MemberRepository memberRepository, EmployeeRepository employeeRepository, MemberService memberService) {
        this.roomPriceRepository = roomPriceRepository;
        this.roomDetailsRepository = roomDetailsRepository;
        this.roomInfoRepository = roomInfoRepository;
        this.roomReservationRepository = roomReservationRepository;
        this.employeeService = employeeService;
        this.memberRepository = memberRepository;
        this.employeeRepository = employeeRepository;
        this.memberService = memberService;
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

    public void modRoomPrice(Long id, Double newPrice) {
        Employee authenticatedEmployee = getAuthenticatedEmployee();
        if(!authenticatedEmployee.getDepartment().toString().equals("Room")){
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }

        RoomInfo roomInfo = roomInfoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 타입의 객실은 존재하지 않습니다."));

        roomInfo.updateRoomPrice(newPrice);
        roomInfoRepository.save(roomInfo);
    }

    public InfoRoomResDto memberReservationRoomCheck(Long id) {
        Employee authenticatedEmployee = getAuthenticatedEmployee();
        String auth = authenticatedEmployee.getDepartment().toString();
        if(!auth.equals("Room") && !auth.equals(Department.Office.toString())){
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }

        RoomReservation roomReservation = roomReservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 예약 정보가 없습니다."));

        InfoRoomResDto infoRoomResDto = roomReservation.toInfoRoomResDto();

        return infoRoomResDto;
    }

    public void memberReservationCncRoomByEmployee(InfoRoomResDto dto) {
        roomReservationRepository.deleteById(dto.getId());
    }
}
