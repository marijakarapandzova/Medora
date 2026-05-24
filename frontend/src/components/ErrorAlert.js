import React from 'react';

function ErrorAlert({ message, onClose }) {
  return (
    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4 flex justify-between items-center">
      <span>{message}</span>
      {onClose && (
        <button onClick={onClose} className="font-bold text-red-700">
          ×
        </button>
      )}
    </div>
  );
}

export default ErrorAlert;
