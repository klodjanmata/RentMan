import React from 'react';
import { Typography, Box } from '@mui/material';

export const InvoicesPage: React.FC = () => {
  return (
    <Box sx={{ textAlign: 'center', py: 4 }}>
      <Typography variant="h4">Invoices Page</Typography>
      <Typography variant="body1">Invoice management will be implemented here</Typography>
    </Box>
  );
};
