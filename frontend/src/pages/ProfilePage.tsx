import React from 'react';
import { Typography, Box } from '@mui/material';

export const ProfilePage: React.FC = () => {
  return (
    <Box sx={{ textAlign: 'center', py: 4 }}>
      <Typography variant="h4">Profile Page</Typography>
      <Typography variant="body1">User profile management will be implemented here</Typography>
    </Box>
  );
};
