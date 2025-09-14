import React from 'react';
import {
  Box,
  Container,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  CardActions,
  Paper,
  Chip,
  Avatar,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
} from '@mui/material';
import {
  DirectionsCar,
  Search,
  History,
  Person,
  Star,
  LocationOn,
  CalendarToday,
  Speed,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useQuery } from 'react-query';
import { companyApi } from '../services/api';

export const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  const { data: featuredCompanies } = useQuery(
    'featuredCompanies',
    companyApi.getFeatured
  );

  const handleSearchClick = () => {
    navigate('/search');
  };

  const handleProfileClick = () => {
    navigate('/profile');
  };

  const handleReservationsClick = () => {
    navigate('/reservations');
  };

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  };

  const quickActions = [
    {
      title: 'Search Vehicles',
      description: 'Find your perfect ride',
      icon: <Search />,
      color: 'primary',
      action: handleSearchClick,
    },
    {
      title: 'My Reservations',
      description: 'View booking history',
      icon: <History />,
      color: 'secondary',
      action: handleReservationsClick,
    },
    {
      title: 'Profile',
      description: 'Manage your account',
      icon: <Person />,
      color: 'success',
      action: handleProfileClick,
    },
  ];

  const stats = [
    { label: 'Total Bookings', value: '0', icon: <CalendarToday /> },
    { label: 'Favorite Companies', value: '0', icon: <Star /> },
    { label: 'Cities Visited', value: '0', icon: <LocationOn /> },
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
              {user?.firstName?.charAt(0) || 'U'}
            </Avatar>
            <Box>
              <Typography variant="h4" component="h1" gutterBottom>
                {getGreeting()}, {user?.firstName || 'User'}!
              </Typography>
              <Typography variant="h6" sx={{ opacity: 0.9 }}>
                Welcome back to RentMan
              </Typography>
            </Box>
          </Box>
        </Container>
      </Box>

      <Container maxWidth="lg" sx={{ py: 4 }}>
        {/* Quick Actions */}
        <Typography variant="h5" component="h2" gutterBottom sx={{ mb: 3 }}>
          Quick Actions
        </Typography>
        <Grid container spacing={3} sx={{ mb: 4 }}>
          {quickActions.map((action, index) => (
            <Grid item xs={12} sm={6} md={4} key={index}>
              <Card
                sx={{
                  height: '100%',
                  cursor: 'pointer',
                  transition: 'transform 0.2s, box-shadow 0.2s',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4,
                  },
                }}
                onClick={action.action}
              >
                <CardContent sx={{ textAlign: 'center', py: 3 }}>
                  <Box
                    sx={{
                      display: 'inline-flex',
                      p: 2,
                      borderRadius: '50%',
                      bgcolor: `${action.color}.light`,
                      color: `${action.color}.contrastText`,
                      mb: 2,
                    }}
                  >
                    {action.icon}
                  </Box>
                  <Typography variant="h6" component="h3" gutterBottom>
                    {action.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {action.description}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>

        <Grid container spacing={4}>
          {/* Left Column */}
          <Grid item xs={12} md={8}>
            {/* Search Section */}
            <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <DirectionsCar sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6" component="h3">
                  Ready to Book?
                </Typography>
              </Box>
              <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                Search from hundreds of vehicles across multiple rental companies
              </Typography>
              <Button
                variant="contained"
                size="large"
                startIcon={<Search />}
                onClick={handleSearchClick}
                sx={{ mr: 2 }}
              >
                Search Vehicles
              </Button>
              <Button
                variant="outlined"
                size="large"
                startIcon={<LocationOn />}
                onClick={handleSearchClick}
              >
                Browse by Location
              </Button>
            </Paper>

            {/* Featured Companies */}
            <Paper elevation={2} sx={{ p: 3 }}>
              <Typography variant="h6" component="h3" gutterBottom>
                Featured Rental Companies
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                Trusted partners with verified vehicles
              </Typography>
              <Grid container spacing={2}>
                {featuredCompanies?.slice(0, 4).map((company) => (
                  <Grid item xs={12} sm={6} key={company.id}>
                    <Card
                      variant="outlined"
                      sx={{
                        cursor: 'pointer',
                        '&:hover': {
                          bgcolor: 'action.hover',
                        },
                      }}
                      onClick={() => navigate(`/search?company=${company.id}`)}
                    >
                      <CardContent sx={{ py: 2 }}>
                        <Typography variant="subtitle1" gutterBottom>
                          {company.companyName}
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
                    </Card>
                  </Grid>
                ))}
              </Grid>
            </Paper>
          </Grid>

          {/* Right Column */}
          <Grid item xs={12} md={4}>
            {/* User Stats */}
            <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
              <Typography variant="h6" component="h3" gutterBottom>
                Your Stats
              </Typography>
              <List dense>
                {stats.map((stat, index) => (
                  <ListItem key={index} sx={{ px: 0 }}>
                    <ListItemIcon sx={{ minWidth: 40 }}>
                      {stat.icon}
                    </ListItemIcon>
                    <ListItemText
                      primary={stat.value}
                      secondary={stat.label}
                    />
                  </ListItem>
                ))}
              </List>
            </Paper>

            {/* Recent Activity */}
            <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
              <Typography variant="h6" component="h3" gutterBottom>
                Recent Activity
              </Typography>
              <Box sx={{ textAlign: 'center', py: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  No recent activity yet
                </Typography>
                <Button
                  variant="text"
                  size="small"
                  onClick={handleSearchClick}
                  sx={{ mt: 1 }}
                >
                  Start exploring
                </Button>
              </Box>
            </Paper>

            {/* Quick Tips */}
            <Paper elevation={2} sx={{ p: 3 }}>
              <Typography variant="h6" component="h3" gutterBottom>
                💡 Quick Tips
              </Typography>
              <List dense>
                <ListItem sx={{ px: 0 }}>
                  <ListItemText
                    primary="Book in advance"
                    secondary="Get better rates and availability"
                  />
                </ListItem>
                <ListItem sx={{ px: 0 }}>
                  <ListItemText
                    primary="Check reviews"
                    secondary="Read customer experiences"
                  />
                </ListItem>
                <ListItem sx={{ px: 0 }}>
                  <ListItemText
                    primary="Compare prices"
                    secondary="Multiple companies available"
                  />
                </ListItem>
              </List>
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </Box>
  );
};
