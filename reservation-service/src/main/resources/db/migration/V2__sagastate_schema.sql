CREATE TABLE IF NOT EXISTS sagastate
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version      int8         NOT NULL,
    type         VARCHAR(100) NOT NULL,
    payload      JSONB        NOT NULL,
    current_step VARCHAR(100),
    step_status  JSONB,
    saga_status  VARCHAR(100)
);
