import React from 'react';
import {
  Box,
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Paper,
  Avatar,
  Chip,
} from '@mui/material';
import {
  DirectionsCar,
  People,
  Build,
  Assessment,
  Receipt,
  BugReport,
  TrendingUp,
  Business,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export const CompanyDashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  };

  const managementCards = [
    {
      title: 'Fleet Management',
      description: 'Manage your vehicle inventory',
      icon: <DirectionsCar />,
      color: 'primary',
      path: '/company/fleet',
    },
    {
      title: 'Employee Management',
      description: 'Manage your team members',
      icon: <People />,
      color: 'secondary',
      path: '/company/employees',
    },
    {
      title: 'Maintenance',
      description: 'Track vehicle maintenance',
      icon: <Build />,
      color: 'warning',
      path: '/company/maintenance',
    },
    {
      title: 'Defects',
      description: 'Report and track defects',
      icon: <BugReport />,
      color: 'error',
      path: '/company/defects',
    },
    {
      title: 'Invoices',
      description: 'Manage billing and invoices',
      icon: <Receipt />,
      color: 'info',
      path: '/company/invoices',
    },
    {
      title: 'Reports',
      description: 'View business analytics',
      icon: <Assessment />,
      color: 'success',
      path: '/company/reports',
    },
  ];

  const stats = [
    { label: 'Total Vehicles', value: '0', icon: <DirectionsCar /> },
    { label: 'Active Employees', value: '0', icon: <People /> },
    { label: 'Monthly Revenue', value: '$0', icon: <TrendingUp /> },
    { label: 'Pending Maintenance', value: '0', icon: <Build /> },
  ];

  return (
    <Box>
      {/* Welcome Header */}
      <Box
        sx={{
          background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
          color: 'white',
          py: 4,
        }}
      >
        <Container maxWidth="lg">
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            <Avatar
              sx={{
                width: 64,
                height: 64,
                mr: 3,
                bgcolor: 'rgba(255,255,255,0.2)',
                fontSize: '1.5rem',
              }}
            >
              <Business />
            </Avatar>
            <Box>
              <Typography variant="h4" component="h1" gutterBottom>
                {getGreeting()}, {user?.firstName || 'Admin'}!
              </Typography>
              <Typography variant="h6" sx={{ opacity: 0.9 }}>
                Welcome to your company dashboard
              </Typography>
              {user?.company && (
                <Chip
                  label={user.company.name}
                  sx={{ mt: 1, bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}
                />
              )}
            </Box>
          </Box>
        </Container>
      </Box>

      <Container maxWidth="lg" sx={{ py: 4 }}>
        {/* Quick Stats */}
        <Typography variant="h5" component="h2" gutterBottom sx={{ mb: 3 }}>
          Company Overview
        </Typography>
        <Grid container spacing={3} sx={{ mb: 4 }}>
          {stats.map((stat, index) => (
            <Grid item xs={12} sm={6} md={3} key={index}>
              <Card>
                <CardContent sx={{ textAlign: 'center' }}>
                  <Box
                    sx={{
                      display: 'inline-flex',
                      p: 2,
                      borderRadius: '50%',
                      bgcolor: 'primary.light',
                      color: 'primary.contrastText',
                      mb: 2,
                    }}
                  >
                    {stat.icon}
                  </Box>
                  <Typography variant="h4" component="div" gutterBottom>
                    {stat.value}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {stat.label}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>

        {/* Management Tools */}
        <Typography variant="h5" component="h2" gutterBottom sx={{ mb: 3 }}>
          Management Tools
        </Typography>
        <Grid container spacing={3}>
          {managementCards.map((card, index) => (
            <Grid item xs={12} sm={6} md={4} key={index}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  transition: 'transform 0.2s, box-shadow 0.2s',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4,
                  },
                }}
              >
                <CardContent sx={{ flexGrow: 1, textAlign: 'center', py: 3 }}>
                  <Box
                    sx={{
                      display: 'inline-flex',
                      p: 2,
                      borderRadius: '50%',
                      bgcolor: `${card.color}.light`,
                      color: `${card.color}.contrastText`,
                      mb: 2,
                    }}
                  >
                    {card.icon}
                  </Box>
                  <Typography variant="h6" component="h3" gutterBottom>
                    {card.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {card.description}
                  </Typography>
                </CardContent>
                <CardActions sx={{ justifyContent: 'center', pb: 2 }}>
                  <Button
                    variant="contained"
                    color={card.color as any}
                    onClick={() => navigate(card.path)}
                  >
                    Manage
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>

        {/* Quick Actions */}
        <Paper elevation={2} sx={{ p: 3, mt: 4 }}>
          <Typography variant="h6" component="h3" gutterBottom>
            Quick Actions
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
            <Button
              variant="contained"
              startIcon={<DirectionsCar />}
              onClick={() => navigate('/company/fleet')}
            >
              Add Vehicle
            </Button>
            <Button
              variant="outlined"
              startIcon={<People />}
              onClick={() => navigate('/company/employees')}
            >
              Add Employee
            </Button>
            <Button
              variant="outlined"
              startIcon={<Assessment />}
              onClick={() => navigate('/company/reports')}
            >
              View Reports
            </Button>
          </Box>
        </Paper>
      </Container>
    </Box>
  );
};
