import React from 'react';
import { Typography, Box } from '@mui/material';

export const ReservationsPage: React.FC = () => {
  return (
    <Box sx={{ textAlign: 'center', py: 4 }}>
      <Typography variant="h4">Reservations Page</Typography>
      <Typography variant="body1">User reservations management will be implemented here</Typography>
    </Box>
  );
};
