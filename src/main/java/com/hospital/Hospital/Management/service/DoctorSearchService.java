package com.hospital.Hospital.Management.service;

import com.hospital.Hospital.Management.dto.DoctorSearchResultDto;
import com.hospital.Hospital.Management.dto.TimeSlotDto;
import com.hospital.Hospital.Management.model.Appointment;
import com.hospital.Hospital.Management.model.DoctorAvailability;
import com.hospital.Hospital.Management.model.Role;
import com.hospital.Hospital.Management.model.User;
import com.hospital.Hospital.Management.model.AppointmentStatus; // <-- THIS IS THE MISSING IMPORT
import com.hospital.Hospital.Management.repository.AppointmentRepository;
import com.hospital.Hospital.Management.repository.DoctorAvailabilityRepository;
import com.hospital.Hospital.Management.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorSearchService {

    private final UserRepository userRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<DoctorSearchResultDto> findDoctorsByCriteria(String specialization, String location, double minRating) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isMember(Role.ROLE_DOCTOR, root.get("roles")));
            predicates.add(root.get("slotDurationInMinutes").isNotNull()); // Only show doctors who have set their availability

            if (specialization != null && !specialization.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("specialization"), specialization));
            }
            if (location != null && !location.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("location"), location));
            }
            if (minRating > 0.0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<User> doctors = userRepository.findAll(spec);
        return doctors.stream()
                .map(this::mapToDoctorSearchResultDto)
                .collect(Collectors.toList());
    }

    private DoctorSearchResultDto mapToDoctorSearchResultDto(User doctor) {
        Map<LocalDate, List<TimeSlotDto>> weeklyAvailability = getWeeklyAvailability(doctor);
        return DoctorSearchResultDto.builder()
                .id(doctor.getId())
                .fullName(doctor.getFullName())
                .specialization(doctor.getSpecialization())
                .location(doctor.getLocation())
                .rating(doctor.getRating())
                .availability(weeklyAvailability)
                .build();
    }

    private Map<LocalDate, List<TimeSlotDto>> getWeeklyAvailability(User doctor) {
        Map<LocalDate, List<TimeSlotDto>> weeklySlots = new LinkedHashMap<>();
        List<DoctorAvailability> rules = availabilityRepository.findByDoctorId(doctor.getId());
        if (rules.isEmpty() || doctor.getSlotDurationInMinutes() == null) {
            return weeklySlots; // Return empty map if doctor has not set availability
        }

        List<Appointment> upcomingAppointments = appointmentRepository.findByDoctorAndStatusIn(
                doctor, List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED_BY_DOCTOR)
        );
        Map<LocalDate, List<LocalTime>> bookedSlotsMap = upcomingAppointments.stream()
                .collect(Collectors.groupingBy(
                        apt -> apt.getAppointmentDateTime().toLocalDate(),
                        Collectors.mapping(apt -> apt.getAppointmentDateTime().toLocalTime(), Collectors.toList())
                ));

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) { // Generate for the next 7 days
            LocalDate currentDate = today.plusDays(i);
            List<TimeSlotDto> dailySlots = generateDailySlots(currentDate, rules, doctor.getSlotDurationInMinutes(), bookedSlotsMap.getOrDefault(currentDate, List.of()));
            if (!dailySlots.isEmpty()) {
                weeklySlots.put(currentDate, dailySlots);
            }
        }
        return weeklySlots;
    }

    private List<TimeSlotDto> generateDailySlots(LocalDate date, List<DoctorAvailability> rules, int slotDuration, List<LocalTime> bookedStartTimes) {
        List<TimeSlotDto> slots = new ArrayList<>();
        java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();

        rules.stream()
                .filter(rule -> rule.getDayOfWeek().name().equals(dayOfWeek.name()))
                .findFirst()
                .ifPresent(rule -> {
                    LocalTime currentTime = rule.getStartTime();
                    while (currentTime.isBefore(rule.getEndTime())) {
                        LocalTime slotEnd = currentTime.plusMinutes(slotDuration);
                        if (slotEnd.isAfter(rule.getEndTime())) break;

                        // Don't show slots that start in the past for today's date
                        if (date.isEqual(LocalDate.now()) && currentTime.isBefore(LocalTime.now())) {
                            currentTime = slotEnd; // Move to the next slot
                            continue;
                        }

                        TimeSlotDto slotDto = TimeSlotDto.builder()
                                .startTime(currentTime)
                                .endTime(slotEnd)
                                .status(bookedStartTimes.contains(currentTime) ? "BOOKED" : "AVAILABLE")
                                .build();

                        slots.add(slotDto);
                        currentTime = slotEnd;
                    }
                });

        return slots;
    }
}