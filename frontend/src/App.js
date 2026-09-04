import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import PatientList from './pages/patients/PatientList';
import PatientDetail from './pages/patients/PatientDetail';
import PatientForm from './pages/patients/PatientForm';
import DoctorList from './pages/doctors/DoctorList';
import DoctorDetail from './pages/doctors/DoctorDetail';
import DoctorForm from './pages/doctors/DoctorForm';
import AppointmentList from './pages/appointments/AppointmentList';
import AppointmentForm from './pages/appointments/AppointmentForm';
import MedicalRecordList from './pages/medical-records/MedicalRecordList';
import MedicalRecordDetail from './pages/medical-records/MedicalRecordDetail';
import BillingList from './pages/billing/BillingList';
import BillingDetail from './pages/billing/BillingDetail';
import ReferralList from './pages/referrals/ReferralList';
import MedicalReportList from './pages/medical-reports/MedicalReportList';
import LabTestList from './pages/lab-tests/LabTestList';
import LabResultForm from './pages/lab-tests/LabResultForm';
import ProcedureList from './pages/procedures/ProcedureList';
import ProcedureResultForm from './pages/procedures/ProcedureResultForm';
import DepartmentList from './pages/departments/DepartmentList';
import DepartmentDetail from './pages/departments/DepartmentDetail';
import DoctorsByDepartment from './pages/departments/DoctorsByDepartment';

function App() {
  const [user, setUser] = useState(null);
  const [refresh, setRefresh] = useState(0);

  useEffect(() => {
    const loadUser = () => {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        try {
          setUser(JSON.parse(userStr));
        } catch (err) {
          console.error('Failed to parse user:', err);
          setUser(null);
        }
      } else {
        setUser(null);
      }
    };

    loadUser();

    // Listen for storage changes (other tabs/windows)
    window.addEventListener('storage', loadUser);

    // Create a custom event listener for same-tab changes
    const handleStorageChange = (e) => {
      console.log('Storage change detected:', e.detail);
      loadUser();
    };
    window.addEventListener('userStorageChange', handleStorageChange);

    return () => {
      window.removeEventListener('storage', loadUser);
      window.removeEventListener('userStorageChange', handleStorageChange);
    };
  }, [refresh]);

  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        {user && <Navbar />}
        <main className={user ? "container mx-auto px-4 py-8" : ""}>
          <Routes>
            <Route path="/login" element={<Login />} />

            <Route path="/" element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            } />

            {/* Patient Routes */}
            <Route path="/patients" element={<ProtectedRoute><PatientList /></ProtectedRoute>} />
            <Route path="/patients/new" element={<ProtectedRoute><PatientForm /></ProtectedRoute>} />
            <Route path="/patients/:id" element={<ProtectedRoute><PatientDetail /></ProtectedRoute>} />
            <Route path="/patients/:id/edit" element={<ProtectedRoute><PatientForm /></ProtectedRoute>} />

            {/* Doctor Routes */}
            <Route path="/doctors" element={<ProtectedRoute><DoctorList /></ProtectedRoute>} />
            <Route path="/doctors/new" element={<ProtectedRoute><DoctorForm /></ProtectedRoute>} />
            <Route path="/doctors/:id" element={<ProtectedRoute><DoctorDetail /></ProtectedRoute>} />
            <Route path="/doctors/:id/edit" element={<ProtectedRoute><DoctorForm /></ProtectedRoute>} />

            {/* Department Routes */}
            <Route path="/departments" element={<ProtectedRoute><DepartmentList /></ProtectedRoute>} />
            <Route path="/departments/:departmentId" element={<ProtectedRoute><DepartmentDetail /></ProtectedRoute>} />
            <Route path="/departments/:departmentId/doctors" element={<ProtectedRoute><DoctorsByDepartment /></ProtectedRoute>} />

            {/* Appointment Routes */}
            <Route path="/appointments" element={<ProtectedRoute><AppointmentList /></ProtectedRoute>} />
            <Route path="/appointments/new" element={<ProtectedRoute><AppointmentForm /></ProtectedRoute>} />

            {/* Medical Record Routes */}
            <Route path="/medical-records" element={<ProtectedRoute><MedicalRecordList /></ProtectedRoute>} />
            <Route path="/medical-records/:id" element={<ProtectedRoute><MedicalRecordDetail /></ProtectedRoute>} />

            {/* Medical Report Routes */}
            <Route path="/medical-reports" element={<ProtectedRoute><MedicalReportList /></ProtectedRoute>} />

            {/* Lab Test Routes */}
            <Route path="/lab-tests" element={<ProtectedRoute><LabTestList /></ProtectedRoute>} />
            <Route path="/lab-tests/results" element={<ProtectedRoute><LabResultForm /></ProtectedRoute>} />

            {/* Procedure Routes */}
            <Route path="/procedures" element={<ProtectedRoute><ProcedureList /></ProtectedRoute>} />
            <Route path="/procedures/results" element={<ProtectedRoute><ProcedureResultForm /></ProtectedRoute>} />

            {/* Billing Routes */}
            <Route path="/billing" element={<ProtectedRoute><BillingList /></ProtectedRoute>} />
            <Route path="/billing/:id" element={<ProtectedRoute><BillingDetail /></ProtectedRoute>} />

            {/* Referral Routes */}
            <Route path="/referrals" element={<ProtectedRoute><ReferralList /></ProtectedRoute>} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
