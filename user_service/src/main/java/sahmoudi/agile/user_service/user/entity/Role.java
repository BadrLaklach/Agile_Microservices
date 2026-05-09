package sahmoudi.agile.user_service.user.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum Role {
    ADMIN,
    DEV,
    PO,
    SM,
    MA;

    @JsonCreator
    public static Role from(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Role.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
