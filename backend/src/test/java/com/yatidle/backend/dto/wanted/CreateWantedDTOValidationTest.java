package com.yatidle.backend.dto.wanted;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateWantedDTOValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsCurrentWantedConditionValues() {
        CreateWantedDTO dto = validDto();
        dto.setConditionLevel("99新及以上");

        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void rejectsLegacyWantedConditionValues() {
        CreateWantedDTO dto = validDto();
        dto.setConditionLevel("99新");

        assertThat(validator.validate(dto))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("conditionLevel");
    }

    private static CreateWantedDTO validDto() {
        CreateWantedDTO dto = new CreateWantedDTO();
        dto.setTitle("求购键盘");
        dto.setCampus("南校园");
        return dto;
    }
}
