package com.hotel.flint.user.employee.controller;

import com.hotel.flint.common.dto.CommonErrorDto;
import com.hotel.flint.common.dto.CommonResDto;
import com.hotel.flint.common.enumdir.Department;
import com.hotel.flint.dining.dto.MenuSaveDto;
import com.hotel.flint.reserve.dining.domain.DiningReservation;
import com.hotel.flint.reserve.dining.dto.ReservationDetailDto;
import com.hotel.flint.reserve.dining.dto.ReservationUpdateDto;
import com.hotel.flint.reserve.dining.repository.DiningReservationRepository;
import com.hotel.flint.reserve.dining.service.DiningReservationService;
import com.hotel.flint.user.employee.dto.DiningMenuDto;
import com.hotel.flint.user.employee.dto.InfoDiningResDto;
import com.hotel.flint.user.employee.dto.MenuSearchDto;
import com.hotel.flint.user.employee.service.EmployeeDiningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/employee/dining")
public class EmployeeDiningController {
    private final EmployeeDiningService employeeDiningService;
    private final DiningReservationService diningReservationService;

    @Autowired
    public EmployeeDiningController(EmployeeDiningService employeeDiningService, DiningReservationService diningReservationService){
        this.employeeDiningService = employeeDiningService;
        this.diningReservationService = diningReservationService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> menuList(
            @RequestParam("department") Department department,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "searchValue", required = false) String searchValue) {

        MenuSearchDto searchDto = new MenuSearchDto();

        if ("menuName".equals(searchType)) {
            searchDto.setMenuName(searchValue);
        } else if ("menuId".equals(searchType) && searchValue != null) {
            try {
                searchDto.setId(Long.parseLong(searchValue));
            } catch (NumberFormatException e) {
                return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), "Invalid menuId format"), HttpStatus.BAD_REQUEST);
            }
        }

        List<DiningMenuDto> dtos = employeeDiningService.getMenuList(department, searchDto);
        try {
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "조회 성공", dtos);
            return new ResponseEntity<>(commonResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.FORBIDDEN.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.FORBIDDEN);
        }
    }


    @PostMapping("/addmenu")
    public ResponseEntity<?> addMenu(@RequestBody MenuSaveDto menuSaveDto) {
        System.out.println(menuSaveDto);
        try {
            CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "메뉴가 성공적으로 생성되었습니다", menuSaveDto.getMenuName());
            employeeDiningService.addDiningMenu(menuSaveDto);
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
        } catch (EntityNotFoundException e){
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        } catch (SecurityException | IllegalArgumentException e){
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.FORBIDDEN.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.FORBIDDEN);
        }

    }

    @PatchMapping("/modmenu/{id}")
    public ResponseEntity<?> modDiningMenu(@PathVariable Long id,
                                        @RequestBody Map<String, Integer> request) {
        int newCost = request.get("cost");
        try {
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "가격이 변경되었습니다", null);
            employeeDiningService.modDiningMenu(id, newCost);
            return new ResponseEntity<>(commonResDto ,HttpStatus.OK);
        } catch (EntityNotFoundException e){
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        } catch (SecurityException | IllegalArgumentException e){
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.FORBIDDEN.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("/delmenu/{id}")
    public ResponseEntity<?> delDiningMenu(@PathVariable Long id){
        try {
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "메뉴가 삭제되었습니다.", null);
            employeeDiningService.delDiningMenu(id);
            return new ResponseEntity<>(commonResDto ,HttpStatus.OK);
        } catch (EntityNotFoundException e){
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        } catch (SecurityException | IllegalArgumentException e){
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.FORBIDDEN.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/reserve")
    public ResponseEntity<?> memberReservationDiningCheck(@RequestParam("id") Long id) {
        try {
            List<InfoDiningResDto> infoDiningResDto = employeeDiningService.memberReservationDiningCheck(id);
            return new ResponseEntity<>(infoDiningResDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e){
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.FORBIDDEN.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/cancel_reserve_dining")
    public ResponseEntity<?> memberReservationCncDiningByEmployee(@RequestParam("id") Long id){
        try{
            ReservationDetailDto dto = employeeDiningService.memberReservationCncDiningByEmployee(id);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }catch (EntityNotFoundException e){
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_GATEWAY);
        }
    }

    // 예약 수정
    @PostMapping("/dining/update/{id}")
    public ResponseEntity<?> reserveDiningUpdate(@PathVariable Long id, @RequestBody ReservationUpdateDto dto){
        try {
            DiningReservation diningReservation = diningReservationService.update(id, dto);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.OK,  "예약 수정 완료", diningReservation.getId());
            return new ResponseEntity<>( commonResDto, HttpStatus.OK );
        }catch (IllegalArgumentException e) {
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }
}
