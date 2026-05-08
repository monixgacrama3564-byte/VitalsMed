<?php
// api/index.php — VitalsMed REST API Router
// TriThread · Sprint 1
// XAMPP path: C:\xampp\htdocs\vitalsmed\api\index.php
// Base URL: http://localhost/vitalsmed/api/

require_once '../config/db.php';

$method = $_SERVER['REQUEST_METHOD'];
$uri    = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$parts  = array_values(array_filter(explode('/', $uri)));
$route  = implode('/', array_slice($parts, array_search('api', $parts) + 1));

// ═══════════════════════════════════════
// ROUTE DISPATCH
// ═══════════════════════════════════════
switch (true) {

    // ─── AUTH ───
    case $route === 'auth/login'    && $method === 'POST': handleLogin();    break;
    case $route === 'auth/register' && $method === 'POST': handleRegister(); break;

    // ─── PATIENTS ───
    case $route === 'patients'              && $method === 'GET':    getPatients();       break;
    case $route === 'patients'              && $method === 'POST':   createPatient();     break;
    case preg_match('/^patients\/(\w+)$/', $route, $m) && $method === 'GET':    getPatient($m[1]);       break;
    case preg_match('/^patients\/(\w+)$/', $route, $m) && $method === 'PUT':    updatePatient($m[1]);    break;
    case preg_match('/^patients\/(\w+)$/', $route, $m) && $method === 'DELETE': deletePatient($m[1]);    break;

    // ─── VITALS ───
    case $route === 'vitals/log'            && $method === 'POST':   logVital();          break;
    case preg_match('/^vitals\/(\w+)$/', $route, $m) && $method === 'GET': getVitals($m[1]); break;
    case $route === 'vitals/alerts'         && $method === 'GET':    getAlerts();         break;

    // ─── MEDICATIONS ───
    case $route === 'patient/medications'   && $method === 'GET':    getMedications();    break;
    case $route === 'medications/create'    && $method === 'POST':   createMedication();  break;
    case preg_match('/^medications\/(\d+)$/', $route, $m) && $method === 'GET':    getMedication($m[1]);    break;
    case preg_match('/^medications\/(\d+)$/', $route, $m) && $method === 'PUT':    updateMedication($m[1]); break;
    case preg_match('/^medications\/(\d+)$/', $route, $m) && $method === 'DELETE': deleteMedication($m[1]); break;

    // ─── ADHERENCE ───
    case $route === 'adherence/confirm'     && $method === 'POST':   confirmDose();       break;
    case preg_match('/^adherence\/(\w+)$/', $route, $m) && $method === 'GET': getAdherence($m[1]); break;

    default: respond(['error' => 'Endpoint not found', 'route' => $route], 404);
}

// ═══════════════════════════════════════
// AUTH HANDLERS
// ═══════════════════════════════════════
function handleLogin() {
    $data = getInput();
    $username = trim($data['username'] ?? '');
    $password = $data['password'] ?? '';

    if (!$username || !$password) respond(['error' => 'Username and password required'], 400);

    $conn = getConnection();
    $stmt = $conn->prepare("SELECT * FROM users WHERE username = ? AND is_active = 1 LIMIT 1");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $user = $stmt->get_result()->fetch_assoc();
    $conn->close();

    if (!$user || !password_verify($password, $user['password']))
        respond(['error' => 'Invalid credentials'], 401);

    unset($user['password']);
    respond(['success' => true, 'user' => $user, 'token' => base64_encode($user['user_id'].':'.$user['role'].':'.time())]);
}

function handleRegister() {
    $data = getInput();
    $required = ['username','password','full_name','role'];
    foreach ($required as $f) { if (empty($data[$f])) respond(['error' => "Field '$f' is required"], 400); }
    if (strlen($data['password']) < 6) respond(['error' => 'Password must be at least 6 characters'], 400);
    if (!in_array($data['role'], ['provider','patient'])) respond(['error' => 'Invalid role'], 400);

    $conn = getConnection();
    $stmt = $conn->prepare("SELECT user_id FROM users WHERE username = ?");
    $stmt->bind_param("s", $data['username']);
    $stmt->execute();
    if ($stmt->get_result()->num_rows > 0) respond(['error' => 'Username already taken'], 409);

    $hash = password_hash($data['password'], PASSWORD_BCRYPT);
    $stmt = $conn->prepare("INSERT INTO users (username, password, full_name, email, role) VALUES (?,?,?,?,?)");
    $email = $data['email'] ?? null;
    $stmt->bind_param("sssss", $data['username'], $hash, $data['full_name'], $email, $data['role']);
    $stmt->execute();
    $id = $conn->insert_id;
    $conn->close();

    respond(['success' => true, 'user_id' => $id, 'message' => 'Account created successfully'], 201);
}

// ═══════════════════════════════════════
// PATIENTS CRUD
// ═══════════════════════════════════════
function getPatients() {
    $conn = getConnection();
    $result = $conn->query("SELECT p.*, u.full_name AS provider_name FROM patients p LEFT JOIN users u ON p.provider_id = u.user_id WHERE p.is_active = 1 ORDER BY p.patient_id");
    respond(['data' => $result->fetch_all(MYSQLI_ASSOC), 'count' => $result->num_rows]);
    $conn->close();
}

function getPatient($id) {
    $conn = getConnection();
    $stmt = $conn->prepare("SELECT p.*, u.full_name AS provider_name FROM patients p LEFT JOIN users u ON p.provider_id = u.user_id WHERE p.patient_id = ?");
    $stmt->bind_param("s", $id);
    $stmt->execute();
    $patient = $stmt->get_result()->fetch_assoc();
    if (!$patient) respond(['error' => 'Patient not found'], 404);
    respond(['data' => $patient]);
    $conn->close();
}

function createPatient() {
    $d = getInput();
    if (empty($d['patient_id']) || empty($d['full_name']) || empty($d['age']) || empty($d['condition_name']))
        respond(['error' => 'patient_id, full_name, age, condition_name are required'], 400);

    $conn = getConnection();
    $stmt = $conn->prepare("INSERT INTO patients (patient_id, full_name, age, condition_name, provider_id) VALUES (?,?,?,?,?)");
    $pid = $d['provider_id'] ?? null;
    $stmt->bind_param("ssisss", $d['patient_id'], $d['full_name'], $d['age'], $d['condition_name'], $pid);
    if (!$stmt->execute()) respond(['error' => $conn->error], 500);
    respond(['success' => true, 'patient_id' => $d['patient_id'], 'message' => 'Patient registered'], 201);
    $conn->close();
}

function updatePatient($id) {
    $d = getInput();
    $conn = getConnection();
    $fields = []; $types = ''; $vals = [];
    foreach (['full_name'=>'s','age'=>'i','condition_name'=>'s','provider_id'=>'i'] as $k => $t) {
        if (isset($d[$k])) { $fields[] = "$k=?"; $types .= $t; $vals[] = $d[$k]; }
    }
    if (!$fields) respond(['error' => 'No fields to update'], 400);
    $types .= 's'; $vals[] = $id;
    $stmt = $conn->prepare("UPDATE patients SET " . implode(',', $fields) . " WHERE patient_id=?");
    $stmt->bind_param($types, ...$vals);
    $stmt->execute();
    respond(['success' => true, 'affected' => $stmt->affected_rows]);
    $conn->close();
}

function deletePatient($id) {
    $conn = getConnection();
    $stmt = $conn->prepare("UPDATE patients SET is_active=0 WHERE patient_id=?");
    $stmt->bind_param("s", $id);
    $stmt->execute();
    respond(['success' => true, 'message' => 'Patient deactivated']);
    $conn->close();
}

// ═══════════════════════════════════════
// VITALS — POST /vitals/log (Sprint 1 Core Endpoint)
// ═══════════════════════════════════════
function logVital() {
    $d = getInput();
    if (empty($d['patient_id'])) respond(['error' => 'patient_id is required'], 400);

    // Validation: human-possible range (rejects HR 0 or 500)
    if (isset($d['heart_rate'])) {
        $hr = (float)$d['heart_rate'];
        if ($hr <= 0 || $hr > 300) respond(['error' => "Heart rate $hr is outside human-possible range (1–300 bpm)"], 422);
    }
    if (isset($d['temperature'])) {
        $t = (float)$d['temperature'];
        if ($t < 30 || $t > 45) respond(['error' => "Temperature $t°C is outside human-possible range (30–45°C)"], 422);
    }

    $conn = getConnection();
    $stmt = $conn->prepare("INSERT INTO biometric_logs (patient_id, heart_rate, temperature, bp_systolic, bp_diastolic, logged_by) VALUES (?,?,?,?,?,?)");
    $hr   = $d['heart_rate']   ?? null;
    $temp = $d['temperature']  ?? null;
    $sys  = $d['bp_systolic']  ?? null;
    $dia  = $d['bp_diastolic'] ?? null;
    $by   = $d['logged_by']    ?? null;
    $stmt->bind_param("sdddii", $d['patient_id'], $hr, $temp, $sys, $dia, $by);
    if (!$stmt->execute()) respond(['error' => $conn->error], 500);
    $logId = $conn->insert_id;

    // Check if alert should fire
    $alerts = [];
    if ($hr !== null && ($hr < 60 || $hr > 100)) $alerts[] = "Heart rate $hr bpm is outside safe zone (60–100)";
    if ($temp !== null && ($temp < 36.1 || $temp > 37.5)) $alerts[] = "Temperature {$temp}°C is outside safe zone (36.1–37.5)";

    respond(['success' => true, 'log_id' => $logId, 'alert_triggered' => !empty($alerts), 'alerts' => $alerts], 201);
    $conn->close();
}

function getVitals($patientId) {
    $conn = getConnection();
    $stmt = $conn->prepare("SELECT * FROM biometric_logs WHERE patient_id=? ORDER BY logged_at DESC LIMIT 50");
    $stmt->bind_param("s", $patientId);
    $stmt->execute();
    respond(['data' => $stmt->get_result()->fetch_all(MYSQLI_ASSOC)]);
    $conn->close();
}

function getAlerts() {
    $conn = getConnection();
    $result = $conn->query("SELECT * FROM biometric_logs WHERE hr_status='Alert' OR temp_status='Alert' ORDER BY logged_at DESC LIMIT 20");
    respond(['data' => $result->fetch_all(MYSQLI_ASSOC), 'count' => $result->num_rows]);
    $conn->close();
}

// ═══════════════════════════════════════
// MEDICATIONS CRUD — GET /patient/medications
// ═══════════════════════════════════════
function getMedications() {
    $patientId = $_GET['patient_id'] ?? null;
    $conn = getConnection();

    // Adherence window check: flag Pending→Late if 2hr window passed
    $conn->query("UPDATE prescription_schedules SET status='Late' WHERE status='Pending' AND next_dose_at < DATE_SUB(NOW(), INTERVAL 2 HOUR)");

    $where = $patientId ? "WHERE ps.patient_id='" . $conn->real_escape_string($patientId) . "'" : '';
    $result = $conn->query("SELECT ps.*, p.full_name AS patient_name FROM prescription_schedules ps JOIN patients p ON ps.patient_id = p.patient_id $where ORDER BY ps.next_dose_at ASC");
    respond(['data' => $result->fetch_all(MYSQLI_ASSOC)]);
    $conn->close();
}

function getMedication($id) {
    $conn = getConnection();
    $stmt = $conn->prepare("SELECT * FROM prescription_schedules WHERE med_id=?");
    $stmt->bind_param("i", $id);
    $stmt->execute();
    $med = $stmt->get_result()->fetch_assoc();
    if (!$med) respond(['error' => 'Medication not found'], 404);
    respond(['data' => $med]);
    $conn->close();
}

function createMedication() {
    $d = getInput();
    $required = ['patient_id','medication_name','dosage','daily_frequency','first_dose_time'];
    foreach ($required as $f) { if (empty($d[$f])) respond(['error' => "Field '$f' is required"], 400); }

    // Calculate next dose time
    $freq = (int)$d['daily_frequency'];
    $nextDose = calculateNextDose($d['first_dose_time'], $freq);

    $conn = getConnection();
    $stmt = $conn->prepare("INSERT INTO prescription_schedules (patient_id, medication_name, dosage, daily_frequency, first_dose_time, next_dose_at, status, prescribed_by) VALUES (?,?,?,?,?,?,?,?)");
    $by = $d['prescribed_by'] ?? null;
    $stmt->bind_param("sssisssi", $d['patient_id'], $d['medication_name'], $d['dosage'], $freq, $d['first_dose_time'], $nextDose, 'Pending', $by);
    if (!$stmt->execute()) respond(['error' => $conn->error], 500);
    respond(['success' => true, 'med_id' => $conn->insert_id, 'next_dose_at' => $nextDose, 'message' => 'Medication schedule created'], 201);
    $conn->close();
}

function updateMedication($id) {
    $d = getInput();
    $conn = getConnection();
    $fields = []; $types = ''; $vals = [];
    foreach (['medication_name'=>'s','dosage'=>'s','daily_frequency'=>'i','status'=>'s','next_dose_at'=>'s'] as $k => $t) {
        if (isset($d[$k])) { $fields[] = "$k=?"; $types .= $t; $vals[] = $d[$k]; }
    }
    if (!$fields) respond(['error' => 'No fields to update'], 400);
    $types .= 'i'; $vals[] = $id;
    $stmt = $conn->prepare("UPDATE prescription_schedules SET " . implode(',', $fields) . " WHERE med_id=?");
    $stmt->bind_param($types, ...$vals);
    $stmt->execute();
    respond(['success' => true, 'affected' => $stmt->affected_rows]);
    $conn->close();
}

function deleteMedication($id) {
    $conn = getConnection();
    $stmt = $conn->prepare("DELETE FROM prescription_schedules WHERE med_id=?");
    $stmt->bind_param("i", $id);
    $stmt->execute();
    respond(['success' => true, 'message' => 'Medication removed']);
    $conn->close();
}

// ═══════════════════════════════════════
// ADHERENCE — "Missed Dose" Flagging
// ═══════════════════════════════════════
function confirmDose() {
    $d = getInput();
    if (empty($d['med_id'])) respond(['error' => 'med_id required'], 400);

    $conn = getConnection();
    $stmt = $conn->prepare("SELECT * FROM prescription_schedules WHERE med_id=?");
    $stmt->bind_param("i", $d['med_id']);
    $stmt->execute();
    $med = $stmt->get_result()->fetch_assoc();
    if (!$med) respond(['error' => 'Medication not found'], 404);

    $now = new DateTime();
    $window = new DateTime($med['next_dose_at']);
    $window->modify('+2 hours');
    $status = $now <= $window ? 'Taken' : 'Late';

    // Update medication status
    $stmt = $conn->prepare("UPDATE prescription_schedules SET status=? WHERE med_id=?");
    $stmt->bind_param("si", $status, $d['med_id']);
    $stmt->execute();

    // Log in adherence table
    $stmt = $conn->prepare("INSERT INTO adherence_log (med_id, patient_id, scheduled_at, confirmed_at, status) VALUES (?,?,?,NOW(),?)");
    $confirmedAt = $now->format('Y-m-d H:i:s');
    $stmt->bind_param("isss", $d['med_id'], $med['patient_id'], $med['next_dose_at'], $status);
    $stmt->execute();

    // Calculate next dose for recurring medication
    $nextDose = calculateNextDose(date('H:i:s', strtotime($med['next_dose_at'])), $med['daily_frequency']);
    $stmt = $conn->prepare("UPDATE prescription_schedules SET status='Pending', next_dose_at=? WHERE med_id=?");
    $stmt->bind_param("si", $nextDose, $d['med_id']);
    $stmt->execute();

    respond(['success' => true, 'adherence_status' => $status, 'next_dose_at' => $nextDose]);
    $conn->close();
}

function getAdherence($patientId) {
    $conn = getConnection();
    $stmt = $conn->prepare("SELECT al.*, ps.medication_name, ps.dosage FROM adherence_log al JOIN prescription_schedules ps ON al.med_id = ps.med_id WHERE al.patient_id=? ORDER BY al.scheduled_at DESC LIMIT 30");
    $stmt->bind_param("s", $patientId);
    $stmt->execute();
    respond(['data' => $stmt->get_result()->fetch_all(MYSQLI_ASSOC)]);
    $conn->close();
}

// ═══════════════════════════════════════
// HELPER: Calculate Next Dose
// ═══════════════════════════════════════
function calculateNextDose($firstDoseTime, $frequency) {
    $intervalHours = 24 / max(1, $frequency);
    $firstDt = new DateTime('today ' . $firstDoseTime);
    $now = new DateTime();

    while ($firstDt <= $now) {
        $firstDt->modify("+{$intervalHours} hours");
    }
    return $firstDt->format('Y-m-d H:i:s');
}
?>
