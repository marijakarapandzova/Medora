SELECT
    plt.performed_test_id,
    lt.test_name,
    u_p.first_name AS patient_first_name,
    u_p.last_name AS patient_last_name,
    u_d.first_name AS doctor_first_name,
    u_d.last_name AS doctor_last_name,
    plt.test_date,
    plt.notes
FROM performed_lab_tests plt
         JOIN lab_tests lt ON plt.test_id = lt.test_id
         JOIN patients p ON plt.patient_id = p.patient_id
         JOIN users u_p ON p.patient_id = u_p.patient_id
         JOIN doctors d ON plt.doctor_id = d.doctor_id
         JOIN users u_d ON d.doctor_id = u_d.doctor_id;

SELECT
    lt.test_id,
    lt.test_name,
    lt.description,
    lt.cost,
    plt.test_date,
    plt.notes
FROM lab_tests lt
         JOIN performed_lab_tests plt ON lt.test_id = plt.test_id
WHERE plt.patient_id = 4 AND plt.test_date = '2026-06-16';


