# Medora Frontend

A modern React-based frontend for the Medora Medical Records Management System.

## Features

- **Patient Management**: Create, view, and manage patient information
- **Doctor Management**: Manage doctor profiles and specializations
- **Appointments**: Schedule and manage medical appointments
- **Medical Records**: Access and search patient medical records
- **Billing**: Manage billing records and payment status
- **Responsive Design**: Works seamlessly on desktop and mobile devices

## Prerequisites

- Node.js (v14 or higher)
- npm or yarn

## Installation

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Create a `.env` file in the frontend directory:
```
REACT_APP_API_URL=http://localhost:8080/api
```

## Running the Application

Start the development server:
```bash
npm start
```

The application will open at `http://localhost:3000`

## Building for Production

```bash
npm run build
```

This creates a production-ready build in the `build` directory.

## Project Structure

```
frontend/
├── public/               # Static assets
├── src/
│   ├── components/      # Reusable React components
│   ├── pages/           # Page components
│   │   ├── patients/
│   │   ├── doctors/
│   │   ├── appointments/
│   │   ├── medical-records/
│   │   └── billing/
│   ├── services/        # API service modules
│   ├── App.js          # Main app component
│   ├── index.js        # React entry point
│   └── index.css       # Global styles
├── package.json
└── tailwind.config.js  # Tailwind CSS configuration
```

## API Endpoints

The frontend communicates with the backend API at:
- Base URL: `http://localhost:8080/api`

Ensure the backend server is running before starting the frontend.

## Technologies Used

- **React 18**: UI library
- **React Router 6**: Client-side routing
- **Axios**: HTTP client
- **Tailwind CSS**: Utility-first CSS framework

## License

This project is part of the Medora Medical Records Management System.
