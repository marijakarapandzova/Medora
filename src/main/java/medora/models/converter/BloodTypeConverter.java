package medora.models.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import medora.models.enums.BloodType;

@Converter(autoApply = true)
public class BloodTypeConverter implements AttributeConverter<BloodType, String> {

    @Override
    public String convertToDatabaseColumn(BloodType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public BloodType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : BloodType.fromValue(dbData);
    }
}