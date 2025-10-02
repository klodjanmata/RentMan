import React, { useState } from 'react';
import {
  Box,
  Container,
  Paper,
  Typography,
  TextField,
  Button,
  Grid,
  Tabs,
  Tab,
  Alert,
  Snackbar,
  Divider,
  Switch,
  FormControlLabel,
  Chip,
} from '@mui/material';
import { useAuth } from '../contexts/AuthContext';
import { authApi } from '../services/api';
import { UpdateProfileRequest } from '../types/auth';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`profile-tabpanel-${index}`}
      aria-labelledby={`profile-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
}

export const ProfilePage: React.FC = () => {
  const { user } = useAuth();
  const [tabValue, setTabValue] = useState(0);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  // Profile form state
  const [profileData, setProfileData] = useState<UpdateProfileRequest>({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    phoneNumber: user?.phoneNumber || '',
    dateOfBirth: '',
    driverLicenseNumber: '',
    licenseExpiryDate: '',
    streetAddress: '',
    city: '',
    state: '',
    postalCode: '',
    country: '',
    emailNotifications: true,
    smsNotifications: false,
    preferredLanguage: 'en',
  });

  // Password form state
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const [loading, setLoading] = useState(false);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleProfileChange = (field: keyof UpdateProfileRequest) => (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    const value = event.target.type === 'checkbox' ? event.target.checked : event.target.value;
    setProfileData((prev) => ({ ...prev, [field]: value }));
  };

  const handlePasswordChange = (field: string) => (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setPasswordData((prev) => ({ ...prev, [field]: event.target.value }));
  };

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;

    setLoading(true);
    try {
      console.log('Updating profile with data:', profileData);
      console.log('User ID:', user.id);
      await authApi.updateProfile(user.id, profileData);
      setSnackbar({
        open: true,
        message: 'Profile updated successfully!',
        severity: 'success',
      });
      // Reload the page to fetch updated user data
      window.location.reload();
    } catch (error: any) {
      console.error('Profile update error:', error);
      console.error('Error response:', error.response);
      setSnackbar({
        open: true,
        message: error.response?.data?.error || error.message || 'Failed to update profile',
        severity: 'error',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setSnackbar({
        open: true,
        message: 'New passwords do not match',
        severity: 'error',
      });
      return;
    }

    if (passwordData.newPassword.length < 6) {
      setSnackbar({
        open: true,
        message: 'Password must be at least 6 characters long',
        severity: 'error',
      });
      return;
    }

    setLoading(true);
    try {
      await authApi.changePassword({
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword,
      });
      setSnackbar({
        open: true,
        message: 'Password changed successfully!',
        severity: 'success',
      });
      setPasswordData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
    } catch (error: any) {
      setSnackbar({
        open: true,
        message: error.response?.data?.error || 'Failed to change password',
        severity: 'error',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCloseSnackbar = () => {
    setSnackbar((prev) => ({ ...prev, open: false }));
  };

  if (!user) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Alert severity="error">Please log in to view your profile</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Paper sx={{ p: 3 }}>
        <Box sx={{ mb: 3 }}>
          <Typography variant="h4" gutterBottom>
            My Profile
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, mt: 2, flexWrap: 'wrap' }}>
            <Chip
              label={user.role}
              color="primary"
              size="small"
            />
            <Chip
              label={user.status}
              color={user.status === 'ACTIVE' ? 'success' : 'default'}
              size="small"
            />
            {user.emailVerified && (
              <Chip
                label="Email Verified"
                color="success"
                size="small"
                variant="outlined"
              />
            )}
            {user.phoneVerified && (
              <Chip
                label="Phone Verified"
                color="success"
                size="small"
                variant="outlined"
              />
            )}
            {user.company && (
              <Chip
                label={user.company.name}
                color="secondary"
                size="small"
                variant="outlined"
              />
            )}
          </Box>
        </Box>

        <Divider sx={{ mb: 3 }} />

        <Tabs value={tabValue} onChange={handleTabChange} aria-label="profile tabs">
          <Tab label="Profile Information" />
          <Tab label="Security & Password" />
          <Tab label="Preferences" />
        </Tabs>

        {/* Profile Information Tab */}
        <TabPanel value={tabValue} index={0}>
          <Box component="form" onSubmit={handleUpdateProfile}>
            <Grid container spacing={3}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="First Name"
                  value={profileData.firstName}
                  onChange={handleProfileChange('firstName')}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Last Name"
                  value={profileData.lastName}
                  onChange={handleProfileChange('lastName')}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Email"
                  value={user.email}
                  disabled
                  helperText="Email cannot be changed"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Phone Number"
                  value={profileData.phoneNumber}
                  onChange={handleProfileChange('phoneNumber')}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Date of Birth"
                  type="date"
                  value={profileData.dateOfBirth}
                  onChange={handleProfileChange('dateOfBirth')}
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Driver License Number"
                  value={profileData.driverLicenseNumber}
                  onChange={handleProfileChange('driverLicenseNumber')}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="License Expiry Date"
                  type="date"
                  value={profileData.licenseExpiryDate}
                  onChange={handleProfileChange('licenseExpiryDate')}
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>

              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                  Address Information
                </Typography>
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Street Address"
                  value={profileData.streetAddress}
                  onChange={handleProfileChange('streetAddress')}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="City"
                  value={profileData.city}
                  onChange={handleProfileChange('city')}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="State/Province"
                  value={profileData.state}
                  onChange={handleProfileChange('state')}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Postal Code"
                  value={profileData.postalCode}
                  onChange={handleProfileChange('postalCode')}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Country"
                  value={profileData.country}
                  onChange={handleProfileChange('country')}
                />
              </Grid>

              <Grid item xs={12}>
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  size="large"
                  disabled={loading}
                  sx={{ mt: 2 }}
                >
                  {loading ? 'Updating...' : 'Update Profile'}
                </Button>
              </Grid>
            </Grid>
          </Box>
        </TabPanel>

        {/* Security & Password Tab */}
        <TabPanel value={tabValue} index={1}>
          <Box component="form" onSubmit={handleChangePassword}>
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <Alert severity="info" sx={{ mb: 2 }}>
                  Password must be at least 6 characters long
                </Alert>
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  type="password"
                  label="Current Password"
                  value={passwordData.currentPassword}
                  onChange={handlePasswordChange('currentPassword')}
                  required
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  type="password"
                  label="New Password"
                  value={passwordData.newPassword}
                  onChange={handlePasswordChange('newPassword')}
                  required
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  type="password"
                  label="Confirm New Password"
                  value={passwordData.confirmPassword}
                  onChange={handlePasswordChange('confirmPassword')}
                  required
                />
              </Grid>
              <Grid item xs={12}>
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  size="large"
                  disabled={loading}
                >
                  {loading ? 'Changing...' : 'Change Password'}
                </Button>
              </Grid>
            </Grid>
          </Box>
        </TabPanel>

        {/* Preferences Tab */}
        <TabPanel value={tabValue} index={2}>
          <Box component="form" onSubmit={handleUpdateProfile}>
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Notifications
                </Typography>
              </Grid>
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={profileData.emailNotifications}
                      onChange={handleProfileChange('emailNotifications')}
                    />
                  }
                  label="Email Notifications"
                />
              </Grid>
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={profileData.smsNotifications}
                      onChange={handleProfileChange('smsNotifications')}
                    />
                  }
                  label="SMS Notifications"
                />
              </Grid>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                  Language
                </Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  select
                  label="Preferred Language"
                  value={profileData.preferredLanguage}
                  onChange={handleProfileChange('preferredLanguage')}
                  SelectProps={{
                    native: true,
                  }}
                >
                  <option value="en">English</option>
                  <option value="es">Español</option>
                  <option value="fr">Français</option>
                  <option value="de">Deutsch</option>
                </TextField>
              </Grid>
              <Grid item xs={12}>
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  size="large"
                  disabled={loading}
                  sx={{ mt: 2 }}
                >
                  {loading ? 'Updating...' : 'Save Preferences'}
                </Button>
              </Grid>
            </Grid>
    </Box>
        </TabPanel>
      </Paper>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Container>
  );
};
