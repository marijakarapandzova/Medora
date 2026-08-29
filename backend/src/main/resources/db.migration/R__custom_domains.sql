CREATE DOMAIN embg_format AS TEXT
    CHECK (VALUE ~ '^\d{13}$');

CREATE DOMAIN email_format AS TEXT
    CHECK (
        VALUE ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
        AND LENGTH(VALUE) <= 254
    );

CREATE DOMAIN phone_number_format AS TEXT
    CHECK (VALUE ~ '^\+?[\d\s\-().]{7,20}$');

CREATE DOMAIN non_negative_cost AS DECIMAL(12,2)
    CHECK (VALUE >= 0);