
BEGIN;

DROP TABLE IF EXISTS medical_record_allergies CASCADE;
DROP TABLE IF EXISTS medical_record_symptoms CASCADE;
DROP TABLE IF EXISTS medical_record_procedures CASCADE;
DROP TABLE IF EXISTS medical_record_lab_results CASCADE;
DROP TABLE IF EXISTS performed_lab_tests CASCADE;
DROP TABLE IF EXISTS performed_procedures CASCADE;
DROP TABLE IF EXISTS medical_report_lab_results CASCADE;
DROP TABLE IF EXISTS prescription_medical_records CASCADE;
DROP TABLE IF EXISTS diagnosis_medical_records CASCADE;
DROP TABLE IF EXISTS doctor_medical_records CASCADE;
DROP TABLE IF EXISTS billing_lab_tests CASCADE;
DROP TABLE IF EXISTS billing_procedures CASCADE;
DROP TABLE IF EXISTS department_procedures CASCADE;
DROP TABLE IF EXISTS diagnosis_procedures CASCADE;
DROP TABLE IF EXISTS specialization_procedures CASCADE;
DROP TABLE IF EXISTS diagnosis_symptoms CASCADE;
DROP TABLE IF EXISTS patient_symptoms CASCADE;
DROP TABLE IF EXISTS allergy_prescription_restrictions CASCADE;
DROP TABLE IF EXISTS patient_allergies CASCADE;
DROP TABLE IF EXISTS symptoms CASCADE;
DROP TABLE IF EXISTS allergies CASCADE;

DROP TABLE IF EXISTS billing CASCADE;
DROP TABLE IF EXISTS medical_report CASCADE;
DROP TABLE IF EXISTS referrals CASCADE;
DROP TABLE IF EXISTS medical_records CASCADE;
DROP TABLE IF EXISTS lab_results CASCADE;
DROP TABLE IF EXISTS lab_tests CASCADE;
DROP TABLE IF EXISTS prescription_restriction CASCADE;
DROP TABLE IF EXISTS prescriptions CASCADE;
DROP TABLE IF EXISTS procedure_results CASCADE;
DROP TABLE IF EXISTS procedures CASCADE;
DROP TABLE IF EXISTS diagnosis CASCADE;
DROP TABLE IF EXISTS appointments CASCADE;
DROP TABLE IF EXISTS lab_technician CASCADE;
DROP TABLE IF EXISTS admin CASCADE;
DROP TABLE IF EXISTS patients CASCADE;
DROP TABLE IF EXISTS doctors CASCADE;
DROP TABLE IF EXISTS departments CASCADE;
DROP TABLE IF EXISTS doctor_specialization CASCADE;
DROP TABLE IF EXISTS doctor_level CASCADE;




CREATE TABLE doctor_level (
                              level_id BIGINT,
                              level TEXT NOT NULL,

                              CONSTRAINT doctor_level_PK
                                  PRIMARY KEY (level_id),

                              CONSTRAINT doctor_level_level_UQ
                                  UNIQUE (level)
);

CREATE TABLE doctor_specialization (
                                       specialization_id BIGINT,
                                       specialization_name TEXT NOT NULL,

                                       CONSTRAINT doctor_specialization_PK
                                           PRIMARY KEY (specialization_id),

                                       CONSTRAINT doctor_specialization_name_UQ
                                           UNIQUE (specialization_name)
);


CREATE TABLE departments (
                             department_id BIGINT,
                             department_name TEXT NOT NULL,

                             CONSTRAINT departments_PK
                                 PRIMARY KEY (department_id),

                             CONSTRAINT departments_name_UQ
                                 UNIQUE (department_name)
);

CREATE TABLE doctors (
                         doctor_id BIGINT,

                         first_name TEXT NOT NULL,
                         last_name TEXT NOT NULL,

                         email_address TEXT NOT NULL,

                         level_id BIGINT NOT NULL,
                         specialization_id BIGINT NOT NULL,
                         department_id BIGINT NOT NULL,

                         CONSTRAINT doctors_PK
                             PRIMARY KEY (doctor_id),

                         CONSTRAINT doctors_email_UQ
                             UNIQUE (email_address),

                         CONSTRAINT doctors_email_chk
                             CHECK (email_address LIKE '%@%'),

                         CONSTRAINT doctors_level_FK
                             FOREIGN KEY (level_id)
                                 REFERENCES doctor_level(level_id)
                                 ON DELETE RESTRICT,

                         CONSTRAINT doctors_specialization_FK
                             FOREIGN KEY (specialization_id)
                                 REFERENCES doctor_specialization(specialization_id)
                                 ON DELETE RESTRICT,

                         CONSTRAINT doctors_department_FK
                             FOREIGN KEY (department_id)
                                 REFERENCES departments(department_id)
                                 ON DELETE RESTRICT
);



CREATE TABLE patients (
                          patient_id BIGINT,

                          first_name TEXT NOT NULL,
                          last_name TEXT NOT NULL,

                          email_address TEXT,
                          date_of_birth DATE NOT NULL,

                          blood_type TEXT,
                          gender TEXT,

                          phone_number TEXT,
                          embg TEXT NOT NULL,

                          CONSTRAINT patients_PK
                              PRIMARY KEY (patient_id),

                          CONSTRAINT patients_email_UQ
                              UNIQUE (email_address),

                          CONSTRAINT patients_embg_UQ
                              UNIQUE (embg),

                          CONSTRAINT patients_gender_chk
                              CHECK (gender IN ('MALE', 'FEMALE')),

                          CONSTRAINT patients_blood_type_chk
                              CHECK (
                                  blood_type IN (
                                                 'A+', 'A-',
                                                 'B+', 'B-',
                                                 'AB+', 'AB-',
                                                 'O+', 'O-'
                                      )
                                  )
);

CREATE TABLE admin (
                       admin_id BIGINT,

                       username TEXT NOT NULL,
                       name TEXT NOT NULL,
                       lastname TEXT NOT NULL,

                       email TEXT NOT NULL,

                       CONSTRAINT admin_PK
                           PRIMARY KEY (admin_id),

                       CONSTRAINT admin_username_UQ
                           UNIQUE (username),

                       CONSTRAINT admin_email_UQ
                           UNIQUE (email),

                       CONSTRAINT admin_email_chk
                           CHECK (email LIKE '%@adminmedora%')
);

CREATE TABLE lab_technician (
                                technician_id BIGINT,

                                username TEXT NOT NULL,
                                name TEXT NOT NULL,
                                lastname TEXT NOT NULL,

                                email TEXT NOT NULL,

                                CONSTRAINT lab_technician_PK
                                    PRIMARY KEY (technician_id),

                                CONSTRAINT lab_technician_username_UQ
                                    UNIQUE (username),

                                CONSTRAINT lab_technician_email_UQ
                                    UNIQUE (email),

                                CONSTRAINT lab_technician_email_chk
                                    CHECK (email LIKE '%@labmedora%')
);



CREATE TABLE appointments (
                              appointment_id BIGINT,

                              appointment_date DATE NOT NULL,
                              appointment_time TIME NOT NULL,

                              status TEXT NOT NULL,

                              patient_id BIGINT NOT NULL,
                              doctor_id BIGINT NOT NULL,

                              CONSTRAINT appointments_PK
                                  PRIMARY KEY (appointment_id),

                              CONSTRAINT appointments_status_chk
                                  CHECK (
                                      status IN (
                                                 'SCHEDULED',
                                                 'COMPLETED',
                                                 'CANCELLED',
                                                 'IN_PROGRESS'
                                          )
                                      ),

                              CONSTRAINT appointments_patient_FK
                                  FOREIGN KEY (patient_id)
                                      REFERENCES patients(patient_id)
                                      ON DELETE RESTRICT,

                              CONSTRAINT appointments_doctor_FK
                                  FOREIGN KEY (doctor_id)
                                      REFERENCES doctors(doctor_id)
                                      ON DELETE RESTRICT
);

CREATE TABLE diagnosis (
                           diagnosis_id BIGINT,

                           name TEXT NOT NULL,
                           description TEXT,

                           patient_id BIGINT NOT NULL,
                           doctor_id BIGINT NOT NULL,

                           CONSTRAINT diagnosis_PK
                               PRIMARY KEY (diagnosis_id),

                           CONSTRAINT diagnosis_patient_FK
                               FOREIGN KEY (patient_id)
                                   REFERENCES patients(patient_id)
                                   ON DELETE RESTRICT,

                           CONSTRAINT diagnosis_doctor_FK
                               FOREIGN KEY (doctor_id)
                                   REFERENCES doctors(doctor_id)
                                   ON DELETE RESTRICT
);




CREATE TABLE procedures (
                            procedure_id BIGINT,

                            procedure_type TEXT NOT NULL,
                            procedure_date DATE NOT NULL,

                            description TEXT,
                            cost DECIMAL NOT NULL,

                            doctor_id BIGINT NOT NULL,
                            diagnosis_id BIGINT NOT NULL,

                            CONSTRAINT procedures_PK
                                PRIMARY KEY (procedure_id),

                            CONSTRAINT procedures_cost_chk
                                CHECK (cost >= 0),

                            CONSTRAINT procedures_type_chk
                                CHECK (length(trim(procedure_type)) > 0),

                            CONSTRAINT procedures_doctor_FK
                                FOREIGN KEY (doctor_id)
                                    REFERENCES doctors(doctor_id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT procedures_diagnosis_FK
                                FOREIGN KEY (diagnosis_id)
                                    REFERENCES diagnosis(diagnosis_id)
                                    ON DELETE RESTRICT
);




CREATE TABLE procedure_results (
                                   result_id BIGINT,

                                   result_description TEXT,
                                   result_date DATE NOT NULL,

                                   procedure_id BIGINT NOT NULL,

                                   CONSTRAINT procedure_results_PK
                                       PRIMARY KEY (result_id),

                                   CONSTRAINT procedure_results_description_chk
                                       CHECK (
                                           result_description IS NULL
                                               OR length(trim(result_description)) > 0
                                           ),

                                   CONSTRAINT procedure_results_procedure_FK
                                       FOREIGN KEY (procedure_id)
                                           REFERENCES procedures(procedure_id)
                                           ON DELETE RESTRICT
);

CREATE TABLE prescriptions (
                               prescription_id BIGINT,

                               medication_name TEXT NOT NULL,

                               CONSTRAINT prescriptions_PK
                                   PRIMARY KEY (prescription_id)
);

CREATE TABLE prescription_restriction (
                                          restriction_id BIGINT,

                                          description TEXT NOT NULL,

                                          prescription_id BIGINT NOT NULL,

                                          CONSTRAINT prescription_restriction_PK
                                              PRIMARY KEY (restriction_id),

                                          CONSTRAINT prescription_restriction_desc_chk
                                              CHECK (length(trim(description)) > 0),

                                          CONSTRAINT prescription_restriction_prescription_FK
                                              FOREIGN KEY (prescription_id)
                                                  REFERENCES prescriptions(prescription_id)
                                                  ON DELETE RESTRICT
);


CREATE TABLE lab_tests (
                           test_id BIGINT,

                           test_name TEXT NOT NULL,
                           description TEXT,
                           cost DECIMAL NOT NULL,

                           CONSTRAINT lab_tests_PK
                               PRIMARY KEY (test_id),

                           CONSTRAINT lab_tests_cost_chk
                               CHECK (cost >= 0),

                           CONSTRAINT lab_tests_name_chk
                               CHECK (length(trim(test_name)) > 0)
);


CREATE TABLE lab_results (
                             result_id BIGINT,

                             results TEXT NOT NULL,
                             result_date DATE NOT NULL,

                             test_id BIGINT NOT NULL,

                             CONSTRAINT lab_results_PK
                                 PRIMARY KEY (result_id),

                             CONSTRAINT lab_results_results_chk
                                 CHECK (length(trim(results)) > 0),

                             CONSTRAINT lab_results_date_chk
                                 CHECK (result_date <= CURRENT_DATE),

                             CONSTRAINT lab_results_test_FK
                                 FOREIGN KEY (test_id)
                                     REFERENCES lab_tests(test_id)
                                     ON DELETE RESTRICT
);

CREATE TABLE medical_records (
                                 record_id BIGINT PRIMARY KEY,

                                 patient_id BIGINT NOT NULL,


                                 FOREIGN KEY (patient_id)
                                     REFERENCES patients(patient_id)
                                     ON DELETE RESTRICT
);




CREATE TABLE allergies (
                           allergy_id BIGINT,

                           name TEXT NOT NULL,
                           allergy_severity TEXT NOT NULL,

                           CONSTRAINT allergies_PK
                               PRIMARY KEY (allergy_id),

                           CONSTRAINT allergies_name_UQ
                               UNIQUE (name),

                           CONSTRAINT allergies_name_chk
                               CHECK (length(trim(name)) > 0),

                           CONSTRAINT allergies_severity_chk
                               CHECK (allergy_severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);


CREATE TABLE symptoms (
                          symptom_id BIGINT,

                          name TEXT NOT NULL,
                          description TEXT,

                          CONSTRAINT symptoms_PK
                              PRIMARY KEY (symptom_id),

                          CONSTRAINT symptoms_name_UQ
                              UNIQUE (name),

                          CONSTRAINT symptoms_name_chk
                              CHECK (length(trim(name)) > 0),

                          CONSTRAINT symptoms_description_chk
                              CHECK (
                                  description IS NULL
                                      OR length(trim(description)) > 0
                                  )
);


CREATE TABLE referrals (

                           referral_id BIGINT PRIMARY KEY,

                           reason TEXT NOT NULL,
                           referral_date DATE NOT NULL,

                           record_id BIGINT NOT NULL,
                           from_doctor_id BIGINT NOT NULL,
                           to_doctor_id BIGINT not null,

                           CONSTRAINT referrals_reason_chk
                               CHECK (length(trim(reason)) > 0),

                           FOREIGN KEY (record_id)
                               REFERENCES medical_records(record_id)
                               ON DELETE RESTRICT,

                           FOREIGN KEY (from_doctor_id)
                               REFERENCES doctors(doctor_id)
                               ON DELETE RESTRICT,

                           FOREIGN KEY (to_doctor_id)
                               REFERENCES doctors(doctor_id)
                               ON DELETE RESTRICT
);

CREATE TABLE medical_report (
                                report_id BIGINT PRIMARY KEY,

                                description TEXT NOT NULL,
                                report_date DATE NOT NULL,

                                record_id BIGINT NOT NULL,
                                doctor_id BIGINT NOT NULL,

                                FOREIGN KEY (record_id)
                                    REFERENCES medical_records(record_id)
                                    ON DELETE RESTRICT,

                                FOREIGN KEY (doctor_id)
                                    REFERENCES doctors(doctor_id)
                                    ON DELETE RESTRICT
);


CREATE TABLE billing (
                         bill_id BIGINT PRIMARY KEY,

                         total_cost DECIMAL NOT NULL,

                         payment_status TEXT NOT NULL,
                         payment_date DATE,

                         record_id BIGINT NOT NULL,
                         admin_id BIGINT NOT NULL,

                         CONSTRAINT billing_cost_chk CHECK (total_cost >= 0),
                         CONSTRAINT billing_status_chk
                             CHECK (payment_status IN ('PENDING', 'PAID', 'CANCELLED')),

                         FOREIGN KEY (record_id)
                             REFERENCES medical_records(record_id)
                             ON DELETE RESTRICT,

                         FOREIGN KEY (admin_id)
                             REFERENCES admin(admin_id)
                             ON DELETE RESTRICT
);




CREATE TABLE patient_allergies (
                                   patient_id BIGINT NOT NULL,
                                   allergy_id BIGINT NOT NULL,

                                   CONSTRAINT patient_allergies_PK
                                       PRIMARY KEY (patient_id, allergy_id),

                                   CONSTRAINT patient_allergies_patient_FK
                                       FOREIGN KEY (patient_id)
                                           REFERENCES patients(patient_id)
                                           ON DELETE RESTRICT,

                                   CONSTRAINT patient_allergies_allergy_FK
                                       FOREIGN KEY (allergy_id)
                                           REFERENCES allergies(allergy_id)
                                           ON DELETE RESTRICT
);



CREATE TABLE allergy_prescription_restrictions (
                                                   allergy_id BIGINT NOT NULL,
                                                   restriction_id BIGINT NOT NULL,

                                                   CONSTRAINT allergy_prescription_restrictions_PK
                                                       PRIMARY KEY (allergy_id, restriction_id),

                                                   CONSTRAINT allergy_prescription_restrictions_allergy_FK
                                                       FOREIGN KEY (allergy_id)
                                                           REFERENCES allergies(allergy_id)
                                                           ON DELETE RESTRICT,

                                                   CONSTRAINT allergy_prescription_restrictions_restriction_FK
                                                       FOREIGN KEY (restriction_id)
                                                           REFERENCES prescription_restriction(restriction_id)
                                                           ON DELETE RESTRICT
);



CREATE TABLE patient_symptoms (
                                  patient_id BIGINT NOT NULL,
                                  symptom_id BIGINT NOT NULL,

                                  CONSTRAINT patient_symptoms_PK
                                      PRIMARY KEY (patient_id, symptom_id),

                                  CONSTRAINT patient_symptoms_patient_FK
                                      FOREIGN KEY (patient_id)
                                          REFERENCES patients(patient_id)
                                          ON DELETE RESTRICT,

                                  CONSTRAINT patient_symptoms_symptom_FK
                                      FOREIGN KEY (symptom_id)
                                          REFERENCES symptoms(symptom_id)
                                          ON DELETE RESTRICT
);




CREATE TABLE diagnosis_symptoms (
                                    diagnosis_id BIGINT NOT NULL,
                                    symptom_id BIGINT NOT NULL,

                                    CONSTRAINT diagnosis_symptoms_PK
                                        PRIMARY KEY (diagnosis_id, symptom_id),

                                    CONSTRAINT diagnosis_symptoms_diagnosis_FK
                                        FOREIGN KEY (diagnosis_id)
                                            REFERENCES diagnosis(diagnosis_id)
                                            ON DELETE RESTRICT,

                                    CONSTRAINT diagnosis_symptoms_symptom_FK
                                        FOREIGN KEY (symptom_id)
                                            REFERENCES symptoms(symptom_id)
                                            ON DELETE RESTRICT
);

CREATE TABLE specialization_procedures (
                                           specialization_id BIGINT NOT NULL,
                                           procedure_id BIGINT NOT NULL,

                                           CONSTRAINT specialization_procedures_PK
                                               PRIMARY KEY (specialization_id, procedure_id),

                                           CONSTRAINT specialization_procedures_specialization_FK
                                               FOREIGN KEY (specialization_id)
                                                   REFERENCES doctor_specialization(specialization_id)
                                                   ON DELETE RESTRICT,

                                           CONSTRAINT specialization_procedures_procedure_FK
                                               FOREIGN KEY (procedure_id)
                                                   REFERENCES procedures(procedure_id)
                                                   ON DELETE RESTRICT
);



CREATE TABLE diagnosis_procedures (
                                      diagnosis_id BIGINT NOT NULL,
                                      procedure_id BIGINT NOT NULL,

                                      CONSTRAINT diagnosis_procedures_PK
                                          PRIMARY KEY (diagnosis_id, procedure_id),

                                      CONSTRAINT diagnosis_procedures_diagnosis_FK
                                          FOREIGN KEY (diagnosis_id)
                                              REFERENCES diagnosis(diagnosis_id)
                                              ON DELETE RESTRICT,

                                      CONSTRAINT diagnosis_procedures_procedure_FK
                                          FOREIGN KEY (procedure_id)
                                              REFERENCES procedures(procedure_id)
                                              ON DELETE RESTRICT
);

CREATE TABLE department_procedures (
                                       department_id BIGINT NOT NULL ,
                                       procedure_id BIGINT NOT NULL,

                                       CONSTRAINT department_procedures_PK
                                           PRIMARY KEY (department_id, procedure_id),

                                       CONSTRAINT department_procedures_department_FK
                                           FOREIGN KEY (department_id)
                                               REFERENCES departments(department_id)
                                               ON DELETE RESTRICT,

                                       CONSTRAINT department_procedures_procedure_FK
                                           FOREIGN KEY (procedure_id)
                                               REFERENCES procedures(procedure_id)
                                               ON DELETE RESTRICT
);


CREATE TABLE billing_procedures (
                                    bill_id BIGINT  NOT NULL,
                                    procedure_id BIGINT  NOT NULL,

                                    CONSTRAINT billing_procedures_PK
                                        PRIMARY KEY (bill_id, procedure_id),

                                    CONSTRAINT billing_procedures_bill_FK
                                        FOREIGN KEY (bill_id)
                                            REFERENCES billing(bill_id)
                                            ON DELETE RESTRICT,

                                    CONSTRAINT billing_procedures_procedure_FK
                                        FOREIGN KEY (procedure_id)
                                            REFERENCES procedures(procedure_id)
                                            ON DELETE RESTRICT
);




CREATE TABLE billing_lab_tests (
                                   bill_id BIGINT NOT NULL,
                                   test_id BIGINT NOT NULL,

                                   CONSTRAINT billing_lab_tests_PK
                                       PRIMARY KEY (bill_id, test_id),

                                   CONSTRAINT billing_lab_tests_bill_FK
                                       FOREIGN KEY (bill_id)
                                           REFERENCES billing(bill_id)
                                           ON DELETE RESTRICT,

                                   CONSTRAINT billing_lab_tests_test_FK
                                       FOREIGN KEY (test_id)
                                           REFERENCES lab_tests(test_id)
                                           ON DELETE RESTRICT
);



CREATE TABLE doctor_medical_records (
                                        doctor_id BIGINT NOT NULL,
                                        record_id BIGINT NOT NULL,

                                        CONSTRAINT doctor_medical_records_PK
                                            PRIMARY KEY (doctor_id, record_id),

                                        CONSTRAINT doctor_medical_records_doctor_FK
                                            FOREIGN KEY (doctor_id)
                                                REFERENCES doctors(doctor_id)
                                                ON DELETE RESTRICT,

                                        CONSTRAINT doctor_medical_records_record_FK
                                            FOREIGN KEY (record_id)
                                                REFERENCES medical_records(record_id)
                                                ON DELETE RESTRICT
);



CREATE TABLE diagnosis_medical_records (
                                           diagnosis_id BIGINT NOT NULL,
                                           record_id BIGINT NOT NULL,

                                           CONSTRAINT diagnosis_medical_records_PK
                                               PRIMARY KEY (diagnosis_id, record_id),

                                           CONSTRAINT diagnosis_medical_records_diagnosis_FK
                                               FOREIGN KEY (diagnosis_id)
                                                   REFERENCES diagnosis(diagnosis_id)
                                                   ON DELETE RESTRICT,

                                           CONSTRAINT diagnosis_medical_records_record_FK
                                               FOREIGN KEY (record_id)
                                                   REFERENCES medical_records(record_id)
                                                   ON DELETE RESTRICT
);


CREATE TABLE prescription_medical_records (
                                              prescription_id BIGINT NOT NULL,
                                              record_id BIGINT NOT NULL,

                                              dosage TEXT NOT NULL,
                                              frequency TEXT NOT NULL,
                                              duration TEXT NOT NULL,

                                              notes TEXT,

                                              CONSTRAINT prescription_medical_records_PK
                                                  PRIMARY KEY (prescription_id, record_id),

                                              CONSTRAINT prescription_medical_records_prescription_FK
                                                  FOREIGN KEY (prescription_id)
                                                      REFERENCES prescriptions(prescription_id)
                                                      ON DELETE RESTRICT,

                                              CONSTRAINT prescription_medical_records_record_FK
                                                  FOREIGN KEY (record_id)
                                                      REFERENCES medical_records(record_id)
                                                      ON DELETE RESTRICT
);



CREATE TABLE medical_report_lab_results (
                                            report_id BIGINT NOT NULL,
                                            result_id BIGINT NOT NULL,

                                            CONSTRAINT medical_report_lab_results_PK
                                                PRIMARY KEY (report_id, result_id),

                                            CONSTRAINT medical_report_lab_results_report_FK
                                                FOREIGN KEY (report_id)
                                                    REFERENCES medical_report(report_id)
                                                    ON DELETE RESTRICT,

                                            CONSTRAINT medical_report_lab_results_result_FK
                                                FOREIGN KEY (result_id)
                                                    REFERENCES lab_results(result_id)
                                                    ON DELETE RESTRICT
);



CREATE TABLE performed_procedures (
                                      performed_id BIGINT,

                                      procedure_id BIGINT NOT NULL,
                                      doctor_id BIGINT NOT NULL,
                                      patient_id BIGINT NOT NULL,
                                      diagnosis_id BIGINT,

                                      procedure_date DATE NOT NULL,
                                      notes TEXT,

                                      CONSTRAINT performed_procedures_PK
                                          PRIMARY KEY (performed_id),

                                      CONSTRAINT performed_procedures_procedure_FK
                                          FOREIGN KEY (procedure_id)
                                              REFERENCES procedures(procedure_id)
                                              ON DELETE RESTRICT,

                                      CONSTRAINT performed_procedures_doctor_FK
                                          FOREIGN KEY (doctor_id)
                                              REFERENCES doctors(doctor_id)
                                              ON DELETE RESTRICT,

                                      CONSTRAINT performed_procedures_patient_FK
                                          FOREIGN KEY (patient_id)
                                              REFERENCES patients(patient_id)
                                              ON DELETE RESTRICT,

                                      CONSTRAINT performed_procedures_diagnosis_FK
                                          FOREIGN KEY (diagnosis_id)
                                              REFERENCES diagnosis(diagnosis_id)
                                              ON DELETE RESTRICT
);

CREATE TABLE performed_lab_tests (
                                     performed_test_id BIGINT,

                                     test_id BIGINT NOT NULL,
                                     patient_id BIGINT NOT NULL,
                                     doctor_id BIGINT NOT NULL,
                                     technician_id BIGINT NOT NULL,

                                     test_date DATE NOT NULL,
                                     notes TEXT,

                                     CONSTRAINT performed_lab_tests_PK
                                         PRIMARY KEY (performed_test_id),

                                     CONSTRAINT performed_lab_tests_test_FK
                                         FOREIGN KEY (test_id)
                                             REFERENCES lab_tests(test_id)
                                             ON DELETE RESTRICT,

                                     CONSTRAINT performed_lab_tests_patient_FK
                                         FOREIGN KEY (patient_id)
                                             REFERENCES patients(patient_id)
                                             ON DELETE RESTRICT,

                                     CONSTRAINT performed_lab_tests_doctor_FK
                                         FOREIGN KEY (doctor_id)
                                             REFERENCES doctors(doctor_id)
                                             ON DELETE RESTRICT,

                                     CONSTRAINT performed_lab_tests_technician_FK
                                         FOREIGN KEY (technician_id)
                                             REFERENCES lab_technician(technician_id)
                                             ON DELETE RESTRICT
);

CREATE TABLE medical_record_lab_results (
                                            record_id BIGINT NOT NULL,
                                            result_id BIGINT NOT NULL,

                                            PRIMARY KEY (record_id, result_id),

                                            FOREIGN KEY (record_id)
                                                REFERENCES medical_records(record_id)
                                                ON DELETE RESTRICT,

                                            FOREIGN KEY (result_id)
                                                REFERENCES lab_results(result_id)
                                                ON DELETE RESTRICT
);




CREATE TABLE medical_record_procedures (
                                           record_id BIGINT NOT NULL,
                                           procedure_id BIGINT NOT NULL,

                                           PRIMARY KEY (record_id, procedure_id),

                                           FOREIGN KEY (record_id)
                                               REFERENCES medical_records(record_id)
                                               ON DELETE RESTRICT,

                                           FOREIGN KEY (procedure_id)
                                               REFERENCES procedures(procedure_id)
                                               ON DELETE RESTRICT
);



CREATE TABLE medical_record_symptoms (
                                         record_id BIGINT NOT NULL,
                                         symptom_id BIGINT NOT NULL,
                                         severity TEXT,

                                         PRIMARY KEY (record_id, symptom_id),

                                         FOREIGN KEY (record_id)
                                             REFERENCES medical_records(record_id)
                                             ON DELETE RESTRICT,

                                         FOREIGN KEY (symptom_id)
                                             REFERENCES symptoms(symptom_id)
                                             ON DELETE RESTRICT
);

CREATE TABLE medical_record_allergies (
                                          record_id BIGINT NOT NULL,
                                          allergy_id BIGINT NOT NULL,
                                          reaction TEXT,
                                          severity TEXT,

                                          PRIMARY KEY (record_id, allergy_id),

                                          FOREIGN KEY (record_id)
                                              REFERENCES medical_records(record_id)
                                              ON DELETE RESTRICT,

                                          FOREIGN KEY (allergy_id)
                                              REFERENCES allergies(allergy_id)
                                              ON DELETE RESTRICT
);


COMMIT;

