import React, { useState } from 'react';
import {
  Box,
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  CircularProgress,
  Divider,
  Card,
  CardContent,
  CardHeader,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { companyApi } from '../services/api';
import { SubscriptionPlan } from '../types/company';

interface CompanyRegistrationData {
  // Company Information
  companyName: string;
  businessRegistrationNumber: string;
  taxId: string;
  companyEmail: string;
  phoneNumber: string;
  website: string;
  streetAddress: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  businessType: string;
  description: string;
  contactPersonName: string;
  contactPersonTitle: string;
  subscriptionPlan: SubscriptionPlan;
  
  // Admin User Information
  adminFirstName: string;
  adminLastName: string;
  adminEmail: string;
  adminPassword: string;
  adminPhoneNumber: string;
  adminStreetAddress?: string;
  adminCity?: string;
  adminState?: string;
  adminPostalCode?: string;
  adminCountry?: string;
  adminDateOfBirth?: string;
  adminDriverLicenseNumber?: string;
  adminLicenseExpiryDate?: string;
}

export const CompanyRegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [formData, setFormData] = useState<CompanyRegistrationData>({
    // Company Information
    companyName: '',
    businessRegistrationNumber: '',
    taxId: '',
    companyEmail: '',
    phoneNumber: '',
    website: '',
    streetAddress: '',
    city: '',
    state: '',
    postalCode: '',
    country: '',
    businessType: '',
    description: '',
    contactPersonName: '',
    contactPersonTitle: '',
    subscriptionPlan: SubscriptionPlan.BASIC,
    
    // Admin User Information
    adminFirstName: '',
    adminLastName: '',
    adminEmail: '',
    adminPassword: '',
    adminPhoneNumber: '',
    adminStreetAddress: '',
    adminCity: '',
    adminState: '',
    adminPostalCode: '',
    adminCountry: '',
    adminDateOfBirth: '',
    adminDriverLicenseNumber: '',
    adminLicenseExpiryDate: '',
  });

  const handleInputChange = (field: keyof CompanyRegistrationData, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (error) setError(null);
  };

  const validateForm = (): boolean => {
    const requiredFields: (keyof CompanyRegistrationData)[] = [
      'companyName',
      'businessRegistrationNumber',
      'taxId',
      'companyEmail',
      'phoneNumber',
      'website',
      'streetAddress',
      'city',
      'state',
      'postalCode',
      'country',
      'contactPersonName',
      'adminFirstName',
      'adminLastName',
      'adminEmail',
      'adminPassword',
      'adminPhoneNumber',
    ];

    for (const field of requiredFields) {
      const value = formData[field];
      if (!value || (typeof value === 'string' && value.trim() === '')) {
        setError(`Please fill in the ${field.replace(/([A-Z])/g, ' $1').toLowerCase()}`);
        return false;
      }
    }

    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.companyEmail)) {
      setError('Please enter a valid company email address');
      return false;
    }

    if (!emailRegex.test(formData.adminEmail)) {
      setError('Please enter a valid admin email address');
      return false;
    }

    // Check if admin email is different from company email
    if (formData.adminEmail === formData.companyEmail) {
      setError('Admin email must be different from company email');
      return false;
    }

    // Password validation
    if (formData.adminPassword.length < 6) {
      setError('Admin password must be at least 6 characters long');
      return false;
    }

    // Website validation
    const websiteRegex = /^https?:\/\/.+/;
    if (!websiteRegex.test(formData.website)) {
      setError('Please enter a valid website URL (starting with http:// or https://)');
      return false;
    }

    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await companyApi.register(formData);
      setSuccess(true);
      
      // Redirect to login after a short delay
      setTimeout(() => {
        navigate('/login');
      }, 3000);

    } catch (err: any) {
      setError(err.response?.data?.error || 'Company registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const subscriptionPlans = [
    { value: SubscriptionPlan.BASIC, label: 'Basic - $99/month', description: 'Up to 50 vehicles, 10 employees' },
    { value: SubscriptionPlan.PROFESSIONAL, label: 'Professional - $299/month', description: 'Up to 200 vehicles, 25 employees' },
    { value: SubscriptionPlan.ENTERPRISE, label: 'Enterprise - $599/month', description: 'Up to 1000 vehicles, 100 employees' },
  ];

  if (success) {
    return (
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
          py: 3,
        }}
      >
        <Paper
          elevation={10}
          sx={{
            p: 4,
            width: '100%',
            maxWidth: 500,
            borderRadius: 2,
            textAlign: 'center',
          }}
        >
          <Typography variant="h4" component="h1" gutterBottom color="success.main">
            🎉 Registration Successful!
          </Typography>
          <Typography variant="h6" gutterBottom>
            Your company and admin account have been created successfully!
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
            Your company application is now under review. You can log in with your admin credentials
            to access your company dashboard and start adding vehicles and employees.
          </Typography>
          <Button
            variant="contained"
            size="large"
            onClick={() => navigate('/login')}
            sx={{ mt: 2 }}
          >
            Go to Login
          </Button>
        </Paper>
      </Box>
    );
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
        py: 4,
      }}
    >
      <Container maxWidth="lg">
        <Paper
          elevation={10}
          sx={{
            p: 4,
            borderRadius: 2,
          }}
        >
          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Typography variant="h3" component="h1" gutterBottom>
              Register Your Company
            </Typography>
            <Typography variant="h6" color="text.secondary">
              Join RentMan as a rental company partner
            </Typography>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit}>
            {/* Company Information */}
            <Card sx={{ mb: 3 }}>
              <CardHeader title="Company Information" />
              <CardContent>
                <Grid container spacing={3}>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Company Name"
                      value={formData.companyName}
                      onChange={(e) => handleInputChange('companyName', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Business Registration Number"
                      value={formData.businessRegistrationNumber}
                      onChange={(e) => handleInputChange('businessRegistrationNumber', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Tax ID"
                      value={formData.taxId}
                      onChange={(e) => handleInputChange('taxId', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Business Type"
                      value={formData.businessType}
                      onChange={(e) => handleInputChange('businessType', e.target.value)}
                      placeholder="e.g., LLC, Corporation, Partnership"
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="Company Description"
                      value={formData.description}
                      onChange={(e) => handleInputChange('description', e.target.value)}
                      multiline
                      rows={3}
                      placeholder="Brief description of your rental business"
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* Contact Information */}
            <Card sx={{ mb: 3 }}>
              <CardHeader title="Contact Information" />
              <CardContent>
                <Grid container spacing={3}>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Company Email Address"
                      type="email"
                      value={formData.companyEmail}
                      onChange={(e) => handleInputChange('companyEmail', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Phone Number"
                      value={formData.phoneNumber}
                      onChange={(e) => handleInputChange('phoneNumber', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="Website"
                      value={formData.website}
                      onChange={(e) => handleInputChange('website', e.target.value)}
                      placeholder="https://www.yourcompany.com"
                      required
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Contact Person Name"
                      value={formData.contactPersonName}
                      onChange={(e) => handleInputChange('contactPersonName', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Contact Person Title"
                      value={formData.contactPersonTitle}
                      onChange={(e) => handleInputChange('contactPersonTitle', e.target.value)}
                      placeholder="e.g., CEO, Manager, Owner"
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* Address Information */}
            <Card sx={{ mb: 3 }}>
              <CardHeader title="Business Address" />
              <CardContent>
                <Grid container spacing={3}>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="Street Address"
                      value={formData.streetAddress}
                      onChange={(e) => handleInputChange('streetAddress', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <TextField
                      fullWidth
                      label="City"
                      value={formData.city}
                      onChange={(e) => handleInputChange('city', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <TextField
                      fullWidth
                      label="State/Province"
                      value={formData.state}
                      onChange={(e) => handleInputChange('state', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <TextField
                      fullWidth
                      label="Postal Code"
                      value={formData.postalCode}
                      onChange={(e) => handleInputChange('postalCode', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="Country"
                      value={formData.country}
                      onChange={(e) => handleInputChange('country', e.target.value)}
                      required
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* Subscription Plan */}
            <Card sx={{ mb: 3 }}>
              <CardHeader title="Subscription Plan" />
              <CardContent>
                <FormControl fullWidth>
                  <InputLabel>Choose Your Plan</InputLabel>
                  <Select
                    value={formData.subscriptionPlan}
                    onChange={(e) => handleInputChange('subscriptionPlan', e.target.value)}
                    label="Choose Your Plan"
                  >
                    {subscriptionPlans.map((plan) => (
                      <MenuItem key={plan.value} value={plan.value}>
                        <Box>
                          <Typography variant="subtitle1">{plan.label}</Typography>
                          <Typography variant="body2" color="text.secondary">
                            {plan.description}
                          </Typography>
                        </Box>
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </CardContent>
            </Card>

            {/* Admin User Information */}
            <Card sx={{ mb: 3 }}>
              <CardHeader title="Admin User Account" />
              <CardContent>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                  Create an admin account to manage your company. This will be your login credentials.
                </Typography>
                <Grid container spacing={3}>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Admin First Name"
                      value={formData.adminFirstName}
                      onChange={(e) => handleInputChange('adminFirstName', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Admin Last Name"
                      value={formData.adminLastName}
                      onChange={(e) => handleInputChange('adminLastName', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Admin Email Address"
                      type="email"
                      value={formData.adminEmail}
                      onChange={(e) => handleInputChange('adminEmail', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Admin Phone Number"
                      value={formData.adminPhoneNumber}
                      onChange={(e) => handleInputChange('adminPhoneNumber', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Admin Password"
                      type="password"
                      value={formData.adminPassword}
                      onChange={(e) => handleInputChange('adminPassword', e.target.value)}
                      required
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Date of Birth"
                      type="date"
                      value={formData.adminDateOfBirth}
                      onChange={(e) => handleInputChange('adminDateOfBirth', e.target.value)}
                      InputLabelProps={{ shrink: true }}
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="Driver License Number"
                      value={formData.adminDriverLicenseNumber}
                      onChange={(e) => handleInputChange('adminDriverLicenseNumber', e.target.value)}
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="License Expiry Date"
                      type="date"
                      value={formData.adminLicenseExpiryDate}
                      onChange={(e) => handleInputChange('adminLicenseExpiryDate', e.target.value)}
                      InputLabelProps={{ shrink: true }}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', mt: 4 }}>
              <Button
                variant="outlined"
                size="large"
                onClick={() => navigate('/login')}
                disabled={loading}
              >
                Back to Login
              </Button>
              <Button
                type="submit"
                variant="contained"
                size="large"
                disabled={loading}
                sx={{ minWidth: 200 }}
              >
                {loading ? <CircularProgress size={24} /> : 'Register Company'}
              </Button>
            </Box>
          </Box>
        </Paper>
      </Container>
    </Box>
  );
};
