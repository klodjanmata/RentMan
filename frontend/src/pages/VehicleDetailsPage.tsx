import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Grid,
  Paper,
  Typography,
  Button,
  Chip,
  Divider,
  Card,
  CardContent,
  TextField,
  Alert,
  CircularProgress,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import {
  CheckCircle,
  Cancel,
  CalendarToday,
  AttachMoney,
  LocationOn,
  Person,
  Speed,
  LocalGasStation,
  DriveEta,
  AirlineSeatReclineNormal,
  Luggage,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { useQuery, useMutation } from 'react-query';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import dayjs, { Dayjs } from 'dayjs';
import { vehicleApi, reservationApi } from '../services/api';
import { useAuth } from '../contexts/AuthContext';

export const VehicleDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user, isAuthenticated } = useAuth();
  
  const action = searchParams.get('action');
  const [showReservationForm, setShowReservationForm] = useState(action === 'reserve');
  
  // Reservation form state
  const [startDate, setStartDate] = useState<Dayjs | null>(null);
  const [endDate, setEndDate] = useState<Dayjs | null>(null);
  const [pickupLocation, setPickupLocation] = useState('');
  const [dropoffLocation, setDropoffLocation] = useState('');
  const [notes, setNotes] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Fetch vehicle details
  const { data: vehicle, isLoading, error: fetchError } = useQuery(
    ['vehicle', id],
    () => vehicleApi.getById(id!),
    { enabled: !!id }
  );

  // Calculate total cost
  const calculateTotalCost = () => {
    if (!startDate || !endDate || !vehicle) return 0;
    const days = endDate.diff(startDate, 'day');
    const rate = typeof vehicle.dailyRate === 'string' ? parseFloat(vehicle.dailyRate) : vehicle.dailyRate;
    return days * rate;
  };

  // Create reservation mutation
  const createReservationMutation = useMutation(
    reservationApi.create,
    {
      onSuccess: (data) => {
        setSuccess('Reservation created successfully!');
        setError(null);
        setTimeout(() => {
          navigate('/reservations');
        }, 2000);
      },
      onError: (err: any) => {
        setError(err.response?.data?.error || 'Failed to create reservation');
        setSuccess(null);
      },
    }
  );

  const handleReserve = () => {
    if (!isAuthenticated) {
      // Save the current URL to redirect back after login
      const returnUrl = window.location.pathname + window.location.search;
      navigate(`/login?returnUrl=${encodeURIComponent(returnUrl)}`);
      return;
    }
    setShowReservationForm(true);
  };

  const handleSubmitReservation = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!startDate || !endDate) {
      setError('Please select start and end dates');
      return;
    }

    if (!pickupLocation) {
      setError('Please enter pickup location');
      return;
    }

    createReservationMutation.mutate({
      vehicleId: parseInt(id!),
      startDate: startDate.format('YYYY-MM-DD'),
      endDate: endDate.format('YYYY-MM-DD'),
      pickupLocation,
      returnLocation: dropoffLocation || pickupLocation,
      insuranceIncluded: false,
      notes: notes || undefined,
    });
  };

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh">
        <CircularProgress />
      </Box>
    );
  }

  if (fetchError || !vehicle) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error">Vehicle not found or failed to load</Alert>
        <Button onClick={() => navigate('/search')} sx={{ mt: 2 }}>
          Back to Search
        </Button>
      </Container>
    );
  }

  const features = [
    { icon: <AirlineSeatReclineNormal />, label: 'Seats', value: vehicle.seatingCapacity },
    { icon: <Luggage />, label: 'Luggage', value: vehicle.luggageCapacity || 'N/A' },
    { icon: <Speed />, label: 'Transmission', value: vehicle.transmission },
    { icon: <LocalGasStation />, label: 'Fuel', value: vehicle.fuelType },
    { icon: <DriveEta />, label: 'Type', value: vehicle.type.replace('_', ' ') },
  ];

  const amenities = [
    { key: 'airConditioning', label: 'Air Conditioning', available: vehicle.airConditioning },
    { key: 'gpsNavigation', label: 'GPS Navigation', available: vehicle.gpsNavigation },
    { key: 'bluetooth', label: 'Bluetooth', available: vehicle.bluetooth },
    { key: 'usbCharging', label: 'USB Charging', available: vehicle.usbCharging },
    { key: 'backupCamera', label: 'Backup Camera', available: vehicle.backupCamera },
    { key: 'parkingSensors', label: 'Parking Sensors', available: vehicle.parkingSensors },
    { key: 'sunroof', label: 'Sunroof', available: vehicle.sunroof },
    { key: 'leatherSeats', label: 'Leather Seats', available: vehicle.leatherSeats },
  ];

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Button onClick={() => navigate('/search')} sx={{ mb: 2 }}>
          ← Back to Search
        </Button>

        <Grid container spacing={4}>
          {/* Left Column - Vehicle Info */}
          <Grid item xs={12} md={8}>
            {/* Vehicle Images */}
            <Paper sx={{ p: 0, mb: 3, overflow: 'hidden' }}>
              {vehicle.imageUrls && vehicle.imageUrls.length > 0 ? (
                <Box
                  component="img"
                  src={vehicle.imageUrls[0]}
                  alt={`${vehicle.make} ${vehicle.model}`}
                  sx={{ width: '100%', height: 400, objectFit: 'cover' }}
                />
              ) : (
                <Box
                  sx={{
                    width: '100%',
                    height: 400,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    bgcolor: 'grey.200',
                  }}
                >
                  <DriveEta sx={{ fontSize: 100, color: 'grey.400' }} />
                </Box>
              )}
            </Paper>

            {/* Vehicle Details */}
            <Paper sx={{ p: 3, mb: 3 }}>
              <Typography variant="h4" gutterBottom>
                {vehicle.make} {vehicle.model} ({vehicle.year})
              </Typography>
              
              <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                <Chip label={vehicle.type.replace('_', ' ')} color="primary" />
                <Chip label={vehicle.status} color="success" />
                {vehicle.isFeatured && <Chip label="Featured" color="warning" />}
              </Box>

              <Typography variant="body1" color="text.secondary" paragraph>
                {vehicle.description || 'A great vehicle for your journey.'}
              </Typography>

              <Divider sx={{ my: 2 }} />

              {/* Key Features Grid */}
              <Typography variant="h6" gutterBottom>
                Key Features
              </Typography>
              <Grid container spacing={2} sx={{ mb: 3 }}>
                {features.map((feature, index) => (
                  <Grid item xs={6} sm={4} key={index}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      {feature.icon}
                      <Box>
                        <Typography variant="caption" color="text.secondary">
                          {feature.label}
                        </Typography>
                        <Typography variant="body2" fontWeight="bold">
                          {feature.value}
                        </Typography>
                      </Box>
                    </Box>
                  </Grid>
                ))}
              </Grid>

              <Divider sx={{ my: 2 }} />

              {/* Amenities */}
              <Typography variant="h6" gutterBottom>
                Amenities
              </Typography>
              <Grid container spacing={1}>
                {amenities.map((amenity) => (
                  <Grid item xs={6} sm={4} key={amenity.key}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      {amenity.available ? (
                        <CheckCircle color="success" fontSize="small" />
                      ) : (
                        <Cancel color="disabled" fontSize="small" />
                      )}
                      <Typography
                        variant="body2"
                        color={amenity.available ? 'text.primary' : 'text.secondary'}
                      >
                        {amenity.label}
                      </Typography>
                    </Box>
                  </Grid>
                ))}
              </Grid>
            </Paper>

            {/* Company Info */}
            {vehicle.company && (
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Rental Company
                </Typography>
                <Typography variant="h5" color="primary">
                  {vehicle.company.companyName}
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 1 }}>
                  <LocationOn fontSize="small" color="action" />
                  <Typography variant="body2">
                    {vehicle.company.city}, {vehicle.company.state}
                  </Typography>
                </Box>
              </Paper>
            )}
          </Grid>

          {/* Right Column - Pricing & Reservation */}
          <Grid item xs={12} md={4}>
            <Paper sx={{ p: 3, position: 'sticky', top: 20 }}>
              <Box sx={{ mb: 3 }}>
                <Typography variant="h3" color="primary">
                  ${vehicle.dailyRate}
                  <Typography component="span" variant="h6" color="text.secondary">
                    /day
                  </Typography>
                </Typography>
              </Box>

              {!showReservationForm ? (
                <>
                  <Button
                    variant="contained"
                    size="large"
                    fullWidth
                    onClick={handleReserve}
                    sx={{ mb: 2 }}
                  >
                    Reserve Now
                  </Button>
                  <Typography variant="caption" color="text.secondary" align="center" display="block">
                    Free cancellation up to 24 hours before pickup
                  </Typography>
                </>
              ) : (
                <Box component="form" onSubmit={handleSubmitReservation}>
                  <Typography variant="h6" gutterBottom>
                    Make a Reservation
                  </Typography>

                  {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                  {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

                  <DatePicker
                    label="Start Date"
                    value={startDate}
                    onChange={setStartDate}
                    minDate={dayjs()}
                    slotProps={{ textField: { fullWidth: true, margin: 'normal' } }}
                  />

                  <DatePicker
                    label="End Date"
                    value={endDate}
                    onChange={setEndDate}
                    minDate={startDate || dayjs()}
                    slotProps={{ textField: { fullWidth: true, margin: 'normal' } }}
                  />

                  <TextField
                    fullWidth
                    margin="normal"
                    label="Pickup Location"
                    value={pickupLocation}
                    onChange={(e) => setPickupLocation(e.target.value)}
                    required
                  />

                  <TextField
                    fullWidth
                    margin="normal"
                    label="Drop-off Location (optional)"
                    value={dropoffLocation}
                    onChange={(e) => setDropoffLocation(e.target.value)}
                    placeholder="Same as pickup"
                  />

                  <TextField
                    fullWidth
                    margin="normal"
                    label="Special Requests (optional)"
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    multiline
                    rows={3}
                  />

                  {startDate && endDate && (
                    <Box sx={{ my: 2, p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
                      <Typography variant="body2" gutterBottom>
                        <strong>Rental Period:</strong> {endDate.diff(startDate, 'day')} days
                      </Typography>
                      <Typography variant="h6" color="primary">
                        Total: ${calculateTotalCost().toFixed(2)}
                      </Typography>
                    </Box>
                  )}

                  <Button
                    type="submit"
                    variant="contained"
                    size="large"
                    fullWidth
                    disabled={createReservationMutation.isLoading}
                    sx={{ mt: 2 }}
                  >
                    {createReservationMutation.isLoading ? 'Processing...' : 'Confirm Reservation'}
                  </Button>

                  <Button
                    variant="outlined"
                    fullWidth
                    onClick={() => setShowReservationForm(false)}
                    sx={{ mt: 1 }}
                  >
                    Cancel
                  </Button>
    </Box>
              )}
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </LocalizationProvider>
  );
};
