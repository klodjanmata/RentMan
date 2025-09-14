import React from 'react';
import {
  Box,
  Typography,
  Button,
  Container,
  Grid,
  Card,
  CardContent,
  CardMedia,
  CardActions,
  Chip,
  Paper,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useQuery } from 'react-query';
import { companyApi } from '../services/api';
import { Company } from '../types/company';

export const HomePage: React.FC = () => {
  const navigate = useNavigate();

  const { data: featuredCompanies, isLoading } = useQuery(
    'featuredCompanies',
    companyApi.getFeatured
  );

  const handleSearchClick = () => {
    navigate('/search');
  };

  const handleCompanyClick = (companyId: number) => {
    navigate(`/search?company=${companyId}`);
  };

  return (
    <Box>
      {/* Hero Section */}
      <Box
        sx={{
          background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
          color: 'white',
          py: 8,
          textAlign: 'center',
        }}
      >
        <Container maxWidth="md">
          <Typography variant="h2" component="h1" gutterBottom>
            Find Your Perfect Ride
          </Typography>
          <Typography variant="h5" component="p" sx={{ mb: 4, opacity: 0.9 }}>
            Discover and book vehicles from trusted rental companies across the platform
          </Typography>
          <Button
            variant="contained"
            size="large"
            onClick={handleSearchClick}
            sx={{
              backgroundColor: 'white',
              color: 'primary.main',
              '&:hover': {
                backgroundColor: 'grey.100',
              },
            }}
          >
            Search Vehicles
          </Button>
        </Container>
      </Box>

      {/* Features Section */}
      <Container maxWidth="lg" sx={{ py: 6 }}>
        <Typography variant="h3" component="h2" textAlign="center" gutterBottom>
          Why Choose RentMan?
        </Typography>
        <Grid container spacing={4} sx={{ mt: 2 }}>
          <Grid item xs={12} md={4}>
            <Paper elevation={3} sx={{ p: 3, textAlign: 'center', height: '100%' }}>
              <Typography variant="h5" gutterBottom>
                🚗 Wide Selection
              </Typography>
              <Typography variant="body1">
                Choose from hundreds of vehicles across multiple rental companies
              </Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} md={4}>
            <Paper elevation={3} sx={{ p: 3, textAlign: 'center', height: '100%' }}>
              <Typography variant="h5" gutterBottom>
                💳 Easy Booking
              </Typography>
              <Typography variant="body1">
                Simple and secure booking process with instant confirmation
              </Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} md={4}>
            <Paper elevation={3} sx={{ p: 3, textAlign: 'center', height: '100%' }}>
              <Typography variant="h5" gutterBottom>
                🛡️ Trusted Partners
              </Typography>
              <Typography variant="body1">
                All rental companies are verified and trusted partners
              </Typography>
            </Paper>
          </Grid>
        </Grid>
      </Container>

      {/* Featured Companies Section */}
      <Box sx={{ backgroundColor: 'grey.50', py: 6 }}>
        <Container maxWidth="lg">
          <Typography variant="h3" component="h2" textAlign="center" gutterBottom>
            Featured Rental Companies
          </Typography>
          {isLoading ? (
            <Typography textAlign="center">Loading featured companies...</Typography>
          ) : (
            <Grid container spacing={3} sx={{ mt: 2 }}>
              {featuredCompanies?.slice(0, 6).map((company: Company) => (
                <Grid item xs={12} sm={6} md={4} key={company.id}>
                  <Card
                    sx={{
                      height: '100%',
                      display: 'flex',
                      flexDirection: 'column',
                      cursor: 'pointer',
                      '&:hover': {
                        transform: 'translateY(-4px)',
                        transition: 'transform 0.2s',
                      },
                    }}
                    onClick={() => handleCompanyClick(company.id)}
                  >
                    <CardContent sx={{ flexGrow: 1 }}>
                      <Typography variant="h6" component="h3" gutterBottom>
                        {company.companyName}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" paragraph>
                        {company.description || 'Professional car rental services'}
                      </Typography>
                      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                        <Chip
                          label={company.subscriptionPlan}
                          size="small"
                          color="primary"
                          variant="outlined"
                        />
                        {company.isVerified && (
                          <Chip
                            label="Verified"
                            size="small"
                            color="success"
                            variant="outlined"
                          />
                        )}
                      </Box>
                    </CardContent>
                    <CardActions>
                      <Button size="small" onClick={() => handleCompanyClick(company.id)}>
                        View Vehicles
                      </Button>
                    </CardActions>
                  </Card>
                </Grid>
              ))}
            </Grid>
          )}
        </Container>
      </Box>

      {/* CTA Section */}
      <Container maxWidth="md" sx={{ py: 6, textAlign: 'center' }}>
        <Typography variant="h4" component="h2" gutterBottom>
          Ready to Start Your Journey?
        </Typography>
        <Typography variant="body1" sx={{ mb: 4 }}>
          Join thousands of satisfied customers who trust RentMan for their car rental needs
        </Typography>
        <Button
          variant="contained"
          size="large"
          onClick={handleSearchClick}
          sx={{ mr: 2 }}
        >
          Search Vehicles Now
        </Button>
        <Button
          variant="outlined"
          size="large"
          onClick={() => navigate('/register')}
          sx={{ mr: 2 }}
        >
          Create Account
        </Button>
        <Button
          variant="outlined"
          size="large"
          onClick={() => navigate('/register-company')}
        >
          Register Company
        </Button>
      </Container>
    </Box>
  );
};
