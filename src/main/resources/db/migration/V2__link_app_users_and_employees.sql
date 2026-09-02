-- 1. Ensure ROLE_EMPLOYEE exists in roles catalog
INSERT INTO roles (name, description, created_at, updated_at)
SELECT 'ROLE_EMPLOYEE', 'Standard Employee self-service workspace access', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'ROLE_EMPLOYEE'
);

-- 2. Add nullable user_id column to employees
ALTER TABLE employees 
ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- 3. Link employees to existing AppUsers by matching email
UPDATE employees e
SET user_id = u.id
FROM app_users u
WHERE LOWER(TRIM(e.email)) = LOWER(TRIM(u.email))
  AND e.user_id IS NULL;

-- 4. Backfill AppUser accounts for employees without user logins
INSERT INTO app_users (
    username, 
    password, 
    email, 
    first_name, 
    last_name, 
    enabled, 
    account_non_locked, 
    created_at, 
    updated_at
)
SELECT 
    COALESCE(NULLIF(TRIM(e.employee_code), ''), SPLIT_PART(e.email, '@', 1)) AS username,
    '$2a$12$1Vw69Ym1wYvG5B9uL5325OUZ35Xl0e1N7i2Yj8xK22Yw78vP8kGz2' AS password,
    LOWER(TRIM(e.email)) AS email,
    e.first_name,
    e.last_name,
    e.active AS enabled,
    TRUE AS account_non_locked,
    CURRENT_TIMESTAMP AS created_at,
    CURRENT_TIMESTAMP AS updated_at
FROM employees e
WHERE e.user_id IS NULL
ON CONFLICT (email) DO NOTHING;

-- 5. Second pass linking for newly inserted AppUsers
UPDATE employees e
SET user_id = u.id
FROM app_users u
WHERE LOWER(TRIM(e.email)) = LOWER(TRIM(u.email))
  AND e.user_id IS NULL;

-- 6. Assign ROLE_EMPLOYEE to all users associated with an employee
INSERT INTO user_roles (user_id, role_id)
SELECT DISTINCT 
    e.user_id, 
    r.id AS role_id
FROM employees e
CROSS JOIN roles r
WHERE r.name = 'ROLE_EMPLOYEE'
  AND e.user_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- 7. Add Unique and Foreign Key constraints
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_employees_user_id'
    ) THEN
        ALTER TABLE employees 
        ADD CONSTRAINT uk_employees_user_id UNIQUE (user_id);
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_employees_user_id'
    ) THEN
        ALTER TABLE employees 
        ADD CONSTRAINT fk_employees_user_id 
        FOREIGN KEY (user_id) REFERENCES app_users(id) 
        ON UPDATE CASCADE 
        ON DELETE RESTRICT;
    END IF;
END $$;