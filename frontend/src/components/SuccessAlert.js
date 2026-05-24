import React, { useEffect } from 'react';

function SuccessAlert({ message, onClose, duration = 3000 }) {
  useEffect(() => {
    if (duration > 0) {
      const timer = setTimeout(onClose, duration);
      return () => clearTimeout(timer);
    }
  }, [duration, onClose]);

  return (
    <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4 flex justify-between items-center">
      <span>{message}</span>
      <button onClick={onClose} className="font-bold text-green-700">
        ×
      </button>
    </div>
  );
}

export default SuccessAlert;
