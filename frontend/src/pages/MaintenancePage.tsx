import React from 'react';
import { Typography, Box } from '@mui/material';

export const MaintenancePage: React.FC = () => {
  return (
    <Box sx={{ textAlign: 'center', py: 4 }}>
      <Typography variant="h4">Maintenance Page</Typography>
      <Typography variant="body1">Vehicle maintenance management will be implemented here</Typography>
    </Box>
  );
};
