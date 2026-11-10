package ro.universitate.erasmus.dto;

import jakarta.validation.constraints.*;
import ro.universitate.erasmus.entity.Semester;

public record ProfileRequest(
    @NotNull Long universityId,
    @NotNull Long facultyId,
    @NotNull Long programId,
    @NotNull @Positive Integer studyYear,
    @NotBlank String academicYear,
    @NotNull Semester semester
) {}
