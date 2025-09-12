import React from 'react';
import { Typography, Box } from '@mui/material';

export const DashboardPage: React.FC = () => {
  return (
    <Box sx={{ textAlign: 'center', py: 4 }}>
      <Typography variant="h4">Dashboard Page</Typography>
      <Typography variant="body1">User dashboard will be implemented here</Typography>
    </Box>
  );
};
