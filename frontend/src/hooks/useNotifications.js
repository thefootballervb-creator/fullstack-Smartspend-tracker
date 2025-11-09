import { useEffect, useRef } from 'react';
import { connectNotifications, disconnectNotifications } from '../services/notificationService';
import toast from 'react-hot-toast';
import AuthService from '../services/auth.service';

export const useNotifications = () => {
  const clientRef = useRef(null);

  useEffect(() => {
    const user = AuthService.getCurrentUser();
    if (!user) return;

    const handleNotification = (data) => {
      if (data.type === 'BUDGET_ALERT') {
        const { spent, budget, message } = data.data || {};
        const msg = message || `Budget limit reached! Spent: ${spent?.toFixed(2)}, Budget: ${budget?.toFixed(2)}`;
        toast.error(msg, { duration: 6000 });
      } else {
        toast.info(data.data?.message || 'New notification', { duration: 4000 });
      }
    };

    clientRef.current = connectNotifications(handleNotification);

    return () => {
      if (clientRef.current) {
        disconnectNotifications();
      }
    };
  }, []);
};

export default useNotifications;

