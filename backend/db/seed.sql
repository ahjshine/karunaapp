-- MedSentinel: Seed Data
-- Demo accounts, patients, wards, permissions, baselines

USE medsentinel;

-- Roles
INSERT INTO roles (role_id, name) VALUES
(1, 'DOCTOR'),
(2, 'NURSE'),
(3, 'BILLING'),
(4, 'RECEPTIONIST'),
(5, 'ADMIN'),
(6, 'REVIEWER');

-- Permissions
INSERT INTO permissions (perm_id, code) VALUES
(1, 'VIEW_DEMOGRAPHICS'),
(2, 'VIEW_CLINICAL'),
(3, 'VIEW_BILLING'),
(4, 'EXPORT'),
(5, 'VIEW_ALERTS'),
(6, 'VIEW_LOGS'),
(7, 'ADMIN');

-- Role-Permission mappings
-- Doctor: demographics, clinical, export
INSERT INTO role_permissions (role_id, perm_id) VALUES
(1, 1), (1, 2), (1, 4);

-- Nurse: demographics, clinical (own ward)
INSERT INTO role_permissions (role_id, perm_id) VALUES
(2, 1), (2, 2);

-- Billing: demographics, billing
INSERT INTO role_permissions (role_id, perm_id) VALUES
(3, 1), (3, 3);

-- Receptionist: demographics only
INSERT INTO role_permissions (role_id, perm_id) VALUES
(4, 1);

-- Admin: demographics, admin
INSERT INTO role_permissions (role_id, perm_id) VALUES
(5, 1), (5, 7);

-- Reviewer: alerts, logs
INSERT INTO role_permissions (role_id, perm_id) VALUES
(6, 5), (6, 6);

-- Wards
INSERT INTO wards (ward_id, name) VALUES
(1, 'Cardiology'),
(2, 'Neurology'),
(3, 'Orthopedics'),
(4, 'Pediatrics'),
(5, 'General Medicine');

-- Users (passwords are BCrypt of 'password123')
-- Generated with BCrypt strength 10.
INSERT INTO users (user_id, username, pw_hash, role_id, ward_id, shift_start, shift_end, status) VALUES
(1, 'dr.asha',     '$2y$10$5iOvNK7pwmTprByYlNVZZ.uljMdYNGOWiqRuLeLa8OM55e4gL0sxe', 1, 1, '08:00:00', '20:00:00', 'ACTIVE'),
(2, 'nurse.priya', '$2y$10$5iOvNK7pwmTprByYlNVZZ.uljMdYNGOWiqRuLeLa8OM55e4gL0sxe', 2, 1, '08:00:00', '20:00:00', 'ACTIVE'),
(3, 'clerk.ravi',  '$2y$10$5iOvNK7pwmTprByYlNVZZ.uljMdYNGOWiqRuLeLa8OM55e4gL0sxe', 3, NULL, '09:00:00', '18:00:00', 'ACTIVE'),
(4, 'rec.neha',    '$2y$10$5iOvNK7pwmTprByYlNVZZ.uljMdYNGOWiqRuLeLa8OM55e4gL0sxe', 4, NULL, '09:00:00', '18:00:00', 'ACTIVE'),
(5, 'admin',       '$2y$10$5iOvNK7pwmTprByYlNVZZ.uljMdYNGOWiqRuLeLa8OM55e4gL0sxe', 5, NULL, '00:00:00', '23:59:59', 'ACTIVE'),
(6, 'rev.meera',   '$2y$10$5iOvNK7pwmTprByYlNVZZ.uljMdYNGOWiqRuLeLa8OM55e4gL0sxe', 6, NULL, '09:00:00', '18:00:00', 'ACTIVE');

-- Patients (50 patients across wards)
INSERT INTO patients (patient_id, name, dob, ward_id) VALUES
(1,  'Arjun Mehta',      '1985-03-15', 1),
(2,  'Sunita Sharma',    '1990-07-22', 1),
(3,  'Vikram Patel',     '1978-11-03', 1),
(4,  'Lakshmi Iyer',     '1965-01-28', 1),
(5,  'Rajesh Kumar',     '1972-09-14', 1),
(6,  'Priya Nair',       '1988-04-07', 1),
(7,  'Deepak Gupta',     '1995-12-19', 1),
(8,  'Ananya Singh',     '1982-06-30', 1),
(9,  'Suresh Reddy',     '1970-02-11', 1),
(10, 'Meena Devi',       '1955-08-25', 1),
(11, 'Karthik Rajan',    '1992-05-17', 2),
(12, 'Divya Menon',      '1987-10-03', 2),
(13, 'Anil Verma',       '1968-03-21', 2),
(14, 'Shweta Joshi',     '1993-07-09', 2),
(15, 'Mohan Das',        '1975-12-14', 2),
(16, 'Kavita Pillai',    '1984-01-30', 2),
(17, 'Raman Krishnan',   '1960-09-06', 2),
(18, 'Pooja Agarwal',    '1991-04-18', 2),
(19, 'Sanjay Mishra',    '1977-11-27', 2),
(20, 'Nisha Chauhan',    '1989-02-08', 2),
(21, 'Amit Tiwari',      '1983-06-12', 3),
(22, 'Rekha Saxena',     '1971-08-29', 3),
(23, 'Vivek Bhatt',      '1996-01-05', 3),
(24, 'Geeta Rao',        '1962-10-17', 3),
(25, 'Harsh Vardhan',    '1988-03-23', 3),
(26, 'Swati Kulkarni',   '1979-07-14', 3),
(27, 'Pramod Shetty',    '1994-12-01', 3),
(28, 'Usha Kapoor',      '1967-05-19', 3),
(29, 'Nitin Deshmukh',   '1985-09-30', 3),
(30, 'Rani Mukherjee',   '1973-04-22', 3),
(31, 'Dev Anand',        '1990-08-11', 4),
(32, 'Sita Ram',         '2020-01-15', 4),
(33, 'Rohan Malhotra',   '2018-06-28', 4),
(34, 'Aarti Pandey',     '2019-03-09', 4),
(35, 'Vishal Dubey',     '2021-11-20', 4),
(36, 'Neha Awasthi',     '2017-07-04', 4),
(37, 'Rahul Banerjee',   '2020-09-16', 4),
(38, 'Maya Hegde',       '2019-12-31', 4),
(39, 'Tarun Bose',       '2022-02-14', 4),
(40, 'Jaya Prakash',     '2018-05-07', 4),
(41, 'Kishore Naidu',    '1981-03-26', 5),
(42, 'Padma Lakshmi',    '1969-10-08', 5),
(43, 'Ravi Shankar',     '1986-06-15', 5),
(44, 'Usha Rani',        '1974-01-29', 5),
(45, 'Ganesh Iyer',      '1997-08-03', 5),
(46, 'Saroja Devi',      '1963-12-12', 5),
(47, 'Manoj Kumar',      '1991-04-27', 5),
(48, 'Lata Mangeshkar',  '1958-09-18', 5),
(49, 'Prakash Raj',      '1980-02-06', 5),
(50, 'Anjali Bhagat',    '1993-11-21', 5);

-- Care assignments (Dr. Asha and Nurse Priya assigned to Cardiology patients 1-10)
INSERT INTO care_assignments (user_id, patient_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5),
(2, 6), (2, 7), (2, 8), (2, 9), (2, 10);

-- Patient records (3 record types per patient = 150 records)
INSERT INTO records (patient_id, type, content)
SELECT p.patient_id, 'DEMOGRAPHIC',
    CONCAT('Name: ', p.name, ', DOB: ', p.dob, ', Ward: ', w.name, ', Blood Group: O+, Emergency Contact: +91-98765-', LPAD(p.patient_id, 5, '0'))
FROM patients p JOIN wards w ON p.ward_id = w.ward_id;

INSERT INTO records (patient_id, type, content)
SELECT p.patient_id, 'CLINICAL',
    CONCAT('Diagnosis: Routine checkup. Vitals: BP 120/80, HR 72, Temp 98.6F. Notes: Patient ', p.name, ' in stable condition. Medications: As prescribed. Follow-up in 2 weeks.')
FROM patients p;

INSERT INTO records (patient_id, type, content)
SELECT p.patient_id, 'BILLING',
    CONCAT('Invoice #INV-', LPAD(p.patient_id, 5, '0'), '. Consultation: Rs 500, Lab tests: Rs 1200, Medications: Rs 800. Total: Rs 2500. Insurance: Star Health. Status: Pending.')
FROM patients p;

-- Role baselines (detection thresholds)
-- Doctor: high baseline for on-ward, moderate off-ward
INSERT INTO role_baselines (role_id, scope, max_records_per_10min, multiplier) VALUES
(1, 'ON_WARD',  50, 1.5),
(1, 'OFF_WARD', 10, 1.5),
(1, 'ANY',      60, 1.5);

-- Nurse: high on-ward (busy clinician), low off-ward
INSERT INTO role_baselines (role_id, scope, max_records_per_10min, multiplier) VALUES
(2, 'ON_WARD',  50, 1.5),
(2, 'OFF_WARD',  5, 1.5),
(2, 'ANY',      55, 1.5);

-- Billing: no clinical access baseline, moderate for billing
INSERT INTO role_baselines (role_id, scope, max_records_per_10min, multiplier) VALUES
(3, 'CLINICAL',  0, 1.0),
(3, 'ANY',      15, 1.5);

-- Receptionist: low baseline
INSERT INTO role_baselines (role_id, scope, max_records_per_10min, multiplier) VALUES
(4, 'ANY', 10, 1.5);

-- Admin: moderate
INSERT INTO role_baselines (role_id, scope, max_records_per_10min, multiplier) VALUES
(5, 'ANY', 20, 1.5);

-- Reviewer: no patient access
INSERT INTO role_baselines (role_id, scope, max_records_per_10min, multiplier) VALUES
(6, 'ANY', 0, 1.0);
