
import './App.css';
import React, { useEffect, useState } from 'react';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import PatientList from './pages/patients/PatientList';
import PatientDetail from './pages/patients/PatientDetail';
import PatientForm from './pages/patients/PatientForm';
import DoctorList from './pages/doctors/DoctorList';
import DoctorDetail from './pages/doctors/DoctorDetail';
import DoctorForm from './pages/doctors/DoctorForm';v
import AppointmentList from './pages/appointments/AppointmentList';
import AppointmentForm from './pages/appointments/AppointmentForm';
import DepartmentList from './pages/departments/DepartmentList';
import DepartmentDetail from './pages/departments/DepartmentDetail';
import DoctorsByDepartment from './pages/departments/DoctorsByDepartment';


function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      setUser(JSON.parse(userStr));
    }
  }, []);

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
            </Routes>
          </main>
        </div>
      </Router>
  );



              }
export default App;
