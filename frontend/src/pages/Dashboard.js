import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { patientService } from '../services/patientService';
import { doctorService } from '../services/doctorService';
import { appointmentService } from '../services/appointmentService';
import { labService } from '../services/labService';
import { billingService } from '../services/billingService';
import { medicalRecordService } from '../services/medicalRecordService';
import Loading from '../components/Loading';

function Dashboard() {
  const [stats, setStats] = useState({
    patients: 0,
    doctors: 0,
    appointments: 0,
  });
  const [userAppointments, setUserAppointments] = useState([]);
  const [doctorInfo, setDoctorInfo] = useState(null);
  const [pendingLabTests, setPendingLabTests] = useState([]);
  const [billings, setBillings] = useState([]);
  const [medicalRecords, setMedicalRecords] = useState([]);
  const [patientBillings, setPatientBillings] = useState([]);
  const [loading, setLoading] = useState(true);
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isPatient = user.role === 'PATIENT';
  const isDoctor = user.role === 'DOCTOR';
  const isLabTechnician = user.role === 'LAB_TECHNICIAN';
  const isBillingAdmin = user.role === 'BILLING_ADMIN';

  useEffect(() => {
    const fetchStats = async () => {
      try {
        if (isPatient) {
          // For patients, fetch their own appointments, medical records, and billing
          const [appointmentsRes, medicalRes, billingRes] = await Promise.all([
            appointmentService.getAppointmentsForPatient(user.patientId),
            medicalRecordService.getMedicalRecordByPatientId(user.patientId),
            billingService.getBillingHistoryForPatient(user.patientId)
          ]);
          setUserAppointments(appointmentsRes.data || []);
          // Handle medical records - could be single object or array
          const medicalData = medicalRes.data;
          const medicalArray = Array.isArray(medicalData) ? medicalData : (medicalData ? [medicalData] : []);
          setMedicalRecords(medicalArray);
          setPatientBillings(billingRes.data || []);
        } else if (isDoctor) {
          // For doctors, fetch their own appointments and full doctor information
          const [appointmentsRes, doctorRes] = await Promise.all([
            appointmentService.getAppointmentsForDoctor(user.doctorId),
            doctorService.getDoctorById(user.doctorId),
          ]);
          setUserAppointments(appointmentsRes.data || []);
          setDoctorInfo(doctorRes.data);
        } else if (isLabTechnician) {
          // For lab technicians, fetch pending lab tests
          const pendingRes = await labService.getPendingLabTests();
          setPendingLabTests(pendingRes.data || []);
        } else if (isBillingAdmin) {
          // For billing admins, fetch all billing records
          const billingsRes = await billingService.getAllBillings();
          setBillings(billingsRes.data || []);
        } else {
          // For admin/staff, fetch all stats
          const [patientsRes, doctorsRes, appointmentsRes] = await Promise.all([
            patientService.getAllPatients(),
            doctorService.getAllDoctors(),
            appointmentService.getAllAppointments(),
          ]);

          setStats({
            patients: patientsRes.data.length,
            doctors: doctorsRes.data.length,
            appointments: appointmentsRes.data.length,
          });
        }
      } catch (error) {
        console.error('Error fetching stats:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, [user.userId, isPatient, isDoctor, isLabTechnician]);

  if (loading) return <Loading />;

  if (isPatient) {
    return (
      <div className="page-wrapper">
        <div className="page-heading">
          <h1 className="page-title">Welcome, <em>{user.firstName} {user.lastName}</em></h1>
          <div className="page-date">Saturday · {new Date().toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}</div>
        </div>
        <div className="divider"></div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Your Information</h2>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '15px' }}>
              <div><strong>Name:</strong> {user.firstName} {user.lastName}</div>
              <div><strong>EMBG:</strong> {user.username}</div>
              <div><strong>Role:</strong> Patient</div>
            </div>
          </div>

          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Quick Actions</h2>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <Link to="/appointments/new" style={{
                display: 'block',
                padding: '8px 12px',
                background: '#e9d5ff',
                color: '#1e1035',
                borderRadius: '6px',
                textAlign: 'center',
                textDecoration: 'none',
                fontSize: '12px',
                fontWeight: '500'
              }} onMouseEnter={(e) => e.currentTarget.style.background = '#d8b4fe'} onMouseLeave={(e) => e.currentTarget.style.background = '#e9d5ff'}>
                + Schedule Appointment
              </Link>
              <Link to="/appointments" style={{
                display: 'block',
                padding: '8px 12px',
                background: '#bfdbfe',
                color: '#1e1035',
                borderRadius: '6px',
                textAlign: 'center',
                textDecoration: 'none',
                fontSize: '12px',
                fontWeight: '400'
              }} onMouseEnter={(e) => e.currentTarget.style.background = '#93c5fd'} onMouseLeave={(e) => e.currentTarget.style.background = '#bfdbfe'}>
                View My Appointments
              </Link>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h2 className="card-title">Your Appointments</h2>
          </div>
          {userAppointments.length === 0 ? (
            <p style={{ color: 'var(--color-neutral-600)', fontSize: '12px' }}>No appointments scheduled</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="table">
                <thead>
                  <tr>
                    <th>Doctor</th>
                    <th>Date</th>
                    <th>Time</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {userAppointments.map(apt => (
                    <tr key={apt.appointmentId}>
                      <td>{apt.doctor?.firstName} {apt.doctor?.lastName}</td>
                      <td>{apt.appointmentDate}</td>
                      <td>{apt.appointmentTime}</td>
                      <td><span className={`badge ${apt.status === 'SCHEDULED' ? 'badge-obs' : apt.status === 'COMPLETED' ? 'badge-stable' : 'badge-critical'}`}>{apt.status}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', alignItems: 'start' }}>
          <div className="card" style={{ paddingTop: '20px', paddingRight: '24px', paddingLeft: '24px', paddingBottom: '20px' }}>
            <div className="card-header" style={{ marginBottom: '4px' }}>
              <h2 className="card-title">Medical Records</h2>
            </div>
            <Link to="/medical-records" style={{
              display: 'block',
              padding: '10px 14px',
              background: '#5b21b6',
              color: 'white',
              borderRadius: '6px',
              textAlign: 'center',
              textDecoration: 'none',
              fontSize: '13px',
              fontWeight: '600',
              marginTop: '14px'
            }} onMouseEnter={(e) => e.currentTarget.style.background = '#4c1d95'} onMouseLeave={(e) => e.currentTarget.style.background = '#5b21b6'}>
              View Medical Records
            </Link>
          </div>

          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Billing Stats</h2>
            </div>
            {patientBillings.length === 0 ? (
              <p style={{ color: 'var(--color-neutral-600)', fontSize: '14px' }}>No billing records</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div style={{ padding: '10px', background: 'var(--color-neutral-200)', borderRadius: '6px', fontSize: '14px' }}>
                  <strong>Total Bills:</strong> {patientBillings.length}
                </div>
                <div style={{ padding: '10px', background: '#d1fae5', borderRadius: '6px', fontSize: '14px' }}>
                  <strong>Paid:</strong> ${patientBillings.filter(b => b.paymentStatus === 'PAID').reduce((sum, b) => sum + (b.totalCost || 0), 0).toFixed(2)}
                </div>
                <div style={{ padding: '10px', background: '#fef3c7', borderRadius: '6px', fontSize: '14px' }}>
                  <strong>Pending:</strong> ${patientBillings.filter(b => b.paymentStatus === 'PENDING').reduce((sum, b) => sum + (b.totalCost || 0), 0).toFixed(2)}
                </div>
                <Link to="/billing" style={{
                  display: 'block',
                  padding: '10px 14px',
                  background: '#5b21b6',
                  color: 'white',
                  borderRadius: '6px',
                  textAlign: 'center',
                  textDecoration: 'none',
                  fontSize: '13px',
                  fontWeight: '600'
                }} onMouseEnter={(e) => e.currentTarget.style.background = '#4c1d95'} onMouseLeave={(e) => e.currentTarget.style.background = '#5b21b6'}>
                  View Billing Details
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }

  if (isDoctor) {
    return (
      <div className="page-wrapper">
        <div className="page-heading">
          <h1 className="page-title">Welcome Dr. <em>{user.firstName} {user.lastName}</em></h1>
          <div className="page-date">Saturday · {new Date().toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}</div>
        </div>
        <div className="divider"></div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Your Information</h2>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '15px' }}>
              <div><strong>Name:</strong> {user.firstName} {user.lastName}</div>
              <div><strong>Email:</strong> {user.username}</div>
              <div><strong>Role:</strong> Doctor</div>
              {doctorInfo && (
                <>
                  <div><strong>Department:</strong> {doctorInfo.department?.departmentName ? doctorInfo.department.departmentName.replace(/_DEPT$/, '').replace(/_/g, ' ').split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()).join(' ') : 'N/A'}</div>
                  <div><strong>Level:</strong> {doctorInfo.level?.level || 'N/A'}</div>
                </>
              )}
            </div>
          </div>

          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Quick Actions</h2>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <Link to="/appointments/new" style={{
                display: 'block',
                padding: '8px 12px',
                background: '#e9d5ff',
                color: '#1e1035',
                borderRadius: '6px',
                textAlign: 'center',
                textDecoration: 'none',
                fontSize: '12px',
                fontWeight: '500'
              }} onMouseEnter={(e) => e.currentTarget.style.background = '#d8b4fe'} onMouseLeave={(e) => e.currentTarget.style.background = '#e9d5ff'}>
                + Schedule Appointment
              </Link>
              <Link to="/appointments" style={{
                display: 'block',
                padding: '8px 12px',
                background: '#bfdbfe',
                color: '#1e1035',
                borderRadius: '6px',
                textAlign: 'center',
                textDecoration: 'none',
                fontSize: '12px',
                fontWeight: '400'
              }} onMouseEnter={(e) => e.currentTarget.style.background = '#93c5fd'} onMouseLeave={(e) => e.currentTarget.style.background = '#bfdbfe'}>
                View My Appointments
              </Link>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h2 className="card-title">Your Appointments</h2>
          </div>
          {userAppointments.length === 0 ? (
            <p style={{ color: 'var(--color-neutral-600)', fontSize: '12px' }}>No appointments scheduled</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="table">
                <thead>
                  <tr>
                    <th>Patient</th>
                    <th>Date</th>
                    <th>Time</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {userAppointments.map(apt => (
                    <tr key={apt.appointmentId}>
                      <td>{apt.patient?.firstName} {apt.patient?.lastName}</td>
                      <td>{apt.appointmentDate}</td>
                      <td>{apt.appointmentTime}</td>
                      <td><span className={`badge ${apt.status === 'SCHEDULED' ? 'badge-obs' : apt.status === 'COMPLETED' ? 'badge-stable' : 'badge-critical'}`}>{apt.status}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    );
  }

  if (isLabTechnician) {
    return (
      <div className="page-wrapper">
        <div className="page-heading">
          <h1 className="page-title">Welcome, <em>{user.firstName} {user.lastName}</em></h1>
          <div className="page-date">Saturday · {new Date().toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}</div>
        </div>
        <div className="divider"></div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Your Information</h2>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '15px' }}>
              <div><strong>Name:</strong> {user.firstName} {user.lastName}</div>
              <div><strong>Username:</strong> {user.username}</div>
              <div><strong>Role:</strong> Lab Technician</div>
            </div>
          </div>

          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Quick Actions</h2>
            </div>
            <Link to="/lab-tests" style={{
              display: 'block',
              padding: '10px 14px',
              background: '#bfdbfe',
              color: '#1e1035',
              borderRadius: '6px',
              textAlign: 'center',
              textDecoration: 'none',
              fontSize: '13px',
              fontWeight: '400'
            }} onMouseEnter={(e) => e.currentTarget.style.background = '#93c5fd'} onMouseLeave={(e) => e.currentTarget.style.background = '#bfdbfe'}>
              View Lab Tests
            </Link>
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h2 className="card-title">Pending Lab Tests ({pendingLabTests.length})</h2>
          </div>
          {pendingLabTests.length === 0 ? (
            <p style={{ color: 'var(--color-neutral-600)', fontSize: '12px' }}>No pending lab tests</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="table">
                <thead>
                  <tr>
                    <th>Test Name</th>
                    <th>Patient</th>
                    <th>Doctor</th>
                    <th>Test Date</th>
                    <th>Notes</th>
                  </tr>
                </thead>
                <tbody>
                  {pendingLabTests.map(test => (
                    <tr key={test.testId}>
                      <td>{test.testName}</td>
                      <td>{test.patientName}</td>
                      <td>{test.doctorName}</td>
                      <td>{test.testDate}</td>
                      <td>{test.notes || 'N/A'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    );
  }

  if (isBillingAdmin) {
    return (
      <div className="page-wrapper">
        <div className="page-heading">
          <h1 className="page-title">Welcome, <em>{user.firstName} {user.lastName}</em></h1>
          <div className="page-date">Saturday · {new Date().toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}</div>
        </div>
        <div className="divider"></div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Your Information</h2>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '15px' }}>
              <div><strong>Name:</strong> {user.firstName} {user.lastName}</div>
              <div><strong>Username:</strong> {user.username}</div>
              <div><strong>Role:</strong> Billing Administrator</div>
            </div>
          </div>

          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Quick Actions</h2>
            </div>
            <Link to="/billing" style={{
              display: 'block',
              padding: '10px 14px',
              background: '#bfdbfe',
              color: '#1e1035',
              borderRadius: '6px',
              textAlign: 'center',
              textDecoration: 'none',
              fontSize: '13px',
              fontWeight: '400'
            }} onMouseEnter={(e) => e.currentTarget.style.background = '#93c5fd'} onMouseLeave={(e) => e.currentTarget.style.background = '#bfdbfe'}>
              View Billing
            </Link>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Quick Links</h2>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <Link to="/doctors" style={{
                display: 'block',
                padding: '10px 14px',
                background: '#e9d5ff',
                color: '#1e1035',
                borderRadius: '6px',
                textDecoration: 'none',
                fontSize: '13px',
                fontWeight: '400'
              }} onMouseEnter={(e) => e.currentTarget.style.background = '#d8b4fe'} onMouseLeave={(e) => e.currentTarget.style.background = '#e9d5ff'}>
                View Doctors
              </Link>
              <Link to="/departments" style={{
                display: 'block',
                padding: '10px 14px',
                background: '#bfdbfe',
                color: '#1e1035',
                borderRadius: '6px',
                textDecoration: 'none',
                fontSize: '13px',
                fontWeight: '400'
              }} onMouseEnter={(e) => e.currentTarget.style.background = '#93c5fd'} onMouseLeave={(e) => e.currentTarget.style.background = '#bfdbfe'}>
                View Departments
              </Link>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h2 className="card-title">Billing Records ({billings.length})</h2>
          </div>
          {billings.length === 0 ? (
            <p style={{ color: 'var(--color-neutral-600)', fontSize: '12px' }}>No billing records</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="table">
                <thead>
                  <tr>
                    <th>Patient</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {billings.map(bill => (
                    <tr key={bill.billId}>
                      <td>{bill.patientName}</td>
                      <td>${bill.totalCost}</td>
                      <td><span className={`badge ${bill.paymentStatus === 'PAID' ? 'badge-paid' : bill.paymentStatus === 'PENDING' ? 'badge-pending' : 'badge-cancelled'}`}>{bill.paymentStatus}</span></td>
                      <td>{bill.paymentDate || 'N/A'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="page-wrapper">
      <div className="page-heading">
        <h1 className="page-title">Welcome, <em>{user.firstName} {user.lastName}</em></h1>
        <div className="page-date">Saturday · {new Date().toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}</div>
      </div>
      <div className="divider"></div>

      <div className="stat-grid">
        <StatCard
          title="Total Patients"
          count={stats.patients}
          delta="+2.5% this week"
          statClass="s1"
        />
        <StatCard
          title="Total Doctors"
          count={stats.doctors}
          delta="+1 this month"
          statClass="s2"
        />
        <StatCard
          title="Appointments Today"
          count={stats.appointments}
          delta="+5 newly scheduled"
          statClass="s3"
        />
        <StatCard
          title="Departments"
          count={5}
          delta="All active"
          statClass="s4"
        />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
        <QuickActionsCard />
        <RecentActivityCard />
      </div>
    </div>
  );
}

function StatCard({ title, count, delta, statClass }) {
  return (
    <div className={`stat-card ${statClass}`}>
      <div className="stat-accent"></div>
      <p className="stat-label">{title}</p>
      <p className="stat-value">{count.toLocaleString()}</p>
      <p className="stat-delta">{delta}</p>
    </div>
  );
}

function QuickActionsCard() {
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  // Lab technicians and billing admins should not see admin quick actions
  if (user.role === 'LAB_TECHNICIAN' || user.role === 'BILLING_ADMIN') {
    return null;
  }

  return (
    <div className="card">
      <div className="card-header">
        <h2 className="card-title">Quick Actions</h2>
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
        <Link to="/patients/new" style={{
          display: 'block',
          padding: '10px 14px',
          background: '#e9d5ff',
          color: '#1e1035',
          borderRadius: '6px',
          textDecoration: 'none',
          transition: 'background 0.2s',
          fontSize: '13px',
          fontWeight: '600'
        }} onMouseEnter={(e) => e.currentTarget.style.background = '#d8b4fe'} onMouseLeave={(e) => e.currentTarget.style.background = '#e9d5ff'}>
          + Add New Patient
        </Link>
        <Link to="/doctors/new" style={{
          display: 'block',
          padding: '10px 14px',
          background: '#bfdbfe',
          color: '#1e1035',
          borderRadius: '6px',
          textDecoration: 'none',
          transition: 'background 0.2s',
          fontSize: '13px',
          fontWeight: '600'
        }} onMouseEnter={(e) => e.currentTarget.style.background = '#93c5fd'} onMouseLeave={(e) => e.currentTarget.style.background = '#bfdbfe'}>
          + Add New Doctor
        </Link>
        <Link to="/appointments/new" style={{
          display: 'block',
          padding: '10px 14px',
          background: '#e9d5ff',
          color: '#1e1035',
          borderRadius: '6px',
          textDecoration: 'none',
          transition: 'background 0.2s',
          fontSize: '13px',
          fontWeight: '600'
        }} onMouseEnter={(e) => e.currentTarget.style.background = '#d8b4fe'} onMouseLeave={(e) => e.currentTarget.style.background = '#e9d5ff'}>
          + Create Appointment
        </Link>
      </div>
    </div>
  );
}

function RecentActivityCard() {
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  // Lab technicians and billing admins should not see admin dashboard cards
  if (user.role === 'LAB_TECHNICIAN' || user.role === 'BILLING_ADMIN') {
    return null;
  }

  return (
    <div className="card">
      <div className="card-header">
        <h2 className="card-title">Recent Activity</h2>
        <button className="card-link">View all</button>
      </div>
      <p style={{ color: 'var(--color-neutral-600)', fontSize: '12px' }}>Activity feed coming soon...</p>
    </div>
  );
}

export default Dashboard;
