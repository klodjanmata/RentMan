import React, { useState } from 'react';
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  Grid,
  Chip,
  Button,
  Divider,
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Tab,
  Tabs,
} from '@mui/material';
import {
  CalendarToday,
  LocationOn,
  DirectionsCar,
  AttachMoney,
  Cancel as CancelIcon,
  CheckCircle,
  Pending,
  HourglassEmpty,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { reservationApi } from '../services/api';
import { Reservation, ReservationStatus } from '../types/reservation';
import { useAuth } from '../contexts/AuthContext';

export const ReservationsPage: React.FC = () => {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [selectedTab, setSelectedTab] = useState(0);
  const [selectedReservation, setSelectedReservation] = useState<Reservation | null>(null);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [cancelReason, setCancelReason] = useState('');

  // Fetch user's reservations
  const { data: reservations, isLoading, error } = useQuery(
    'userReservations',
    reservationApi.getUserReservations
  );

  // Cancel reservation mutation
  const cancelMutation = useMutation(
    (id: string) => reservationApi.cancel(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('userReservations');
        setCancelDialogOpen(false);
        setSelectedReservation(null);
        setCancelReason('');
      },
    }
  );

  const handleCancelClick = (reservation: Reservation) => {
    setSelectedReservation(reservation);
    setCancelDialogOpen(true);
  };

  const handleConfirmCancel = () => {
    if (selectedReservation) {
      cancelMutation.mutate(selectedReservation.id.toString());
    }
  };

  const getStatusIcon = (status: ReservationStatus) => {
    switch (status) {
      case ReservationStatus.CONFIRMED:
        return <CheckCircle color="success" />;
      case ReservationStatus.PENDING:
        return <Pending color="warning" />;
      case ReservationStatus.IN_PROGRESS:
        return <HourglassEmpty color="info" />;
      case ReservationStatus.COMPLETED:
        return <CheckCircle color="primary" />;
      case ReservationStatus.CANCELLED:
        return <CancelIcon color="error" />;
      default:
        return <Pending />;
    }
  };

  const getStatusColor = (status: ReservationStatus): "default" | "primary" | "secondary" | "error" | "info" | "success" | "warning" => {
    switch (status) {
      case ReservationStatus.CONFIRMED:
        return 'success';
      case ReservationStatus.PENDING:
        return 'warning';
      case ReservationStatus.IN_PROGRESS:
        return 'info';
      case ReservationStatus.COMPLETED:
        return 'primary';
      case ReservationStatus.CANCELLED:
        return 'error';
      default:
        return 'default';
    }
  };

  const filterReservations = (reservations: Reservation[]) => {
    switch (selectedTab) {
      case 0: // All
        return reservations;
      case 1: // Upcoming
        return reservations.filter(r => 
          r.status === ReservationStatus.CONFIRMED || r.status === ReservationStatus.PENDING
        );
      case 2: // Active
        return reservations.filter(r => r.status === ReservationStatus.IN_PROGRESS);
      case 3: // Completed
        return reservations.filter(r => r.status === ReservationStatus.COMPLETED);
      case 4: // Cancelled
        return reservations.filter(r => r.status === ReservationStatus.CANCELLED);
      default:
        return reservations;
    }
  };

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh">
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error">Failed to load reservations. Please try again later.</Alert>
      </Container>
    );
  }

  const filteredReservations = reservations ? filterReservations(reservations) : [];

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        My Reservations
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        View and manage your vehicle reservations
      </Typography>

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs value={selectedTab} onChange={(e, newValue) => setSelectedTab(newValue)}>
          <Tab label={`All (${reservations?.length || 0})`} />
          <Tab label="Upcoming" />
          <Tab label="Active" />
          <Tab label="Completed" />
          <Tab label="Cancelled" />
        </Tabs>
      </Box>

      {/* Reservations List */}
      {filteredReservations.length === 0 ? (
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            No reservations found
          </Typography>
          <Typography variant="body2" color="text.secondary" paragraph>
            {selectedTab === 0 
              ? "You haven't made any reservations yet"
              : "No reservations in this category"}
          </Typography>
          <Button variant="contained" href="/search" sx={{ mt: 2 }}>
            Search Vehicles
          </Button>
        </Box>
      ) : (
        <Grid container spacing={3}>
          {filteredReservations.map((reservation) => (
            <Grid item xs={12} key={reservation.id}>
              <Card>
                <CardContent>
                  <Grid container spacing={3}>
                    {/* Vehicle Info */}
                    <Grid item xs={12} md={3}>
                      {reservation.vehicle?.imageUrls && reservation.vehicle.imageUrls.length > 0 ? (
                        <Box
                          component="img"
                          src={reservation.vehicle.imageUrls[0]}
                          alt={`${reservation.vehicle.make} ${reservation.vehicle.model}`}
                          sx={{
                            width: '100%',
                            height: 150,
                            objectFit: 'cover',
                            borderRadius: 1,
                          }}
                        />
                      ) : (
                        <Box
                          sx={{
                            width: '100%',
                            height: 150,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            bgcolor: 'grey.200',
                            borderRadius: 1,
                          }}
                        >
                          <DirectionsCar sx={{ fontSize: 60, color: 'grey.400' }} />
                        </Box>
                      )}
                    </Grid>

                    {/* Reservation Details */}
                    <Grid item xs={12} md={6}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                        <Typography variant="h6">
                          {reservation.vehicle?.make} {reservation.vehicle?.model} ({reservation.vehicle?.year})
                        </Typography>
                        <Chip
                          icon={getStatusIcon(reservation.status)}
                          label={reservation.status}
                          color={getStatusColor(reservation.status)}
                          size="small"
                        />
                      </Box>

                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        Reservation #{reservation.reservationNumber}
                      </Typography>

                      <Box sx={{ mt: 2 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                          <CalendarToday fontSize="small" color="action" />
                          <Typography variant="body2">
                            {new Date(reservation.startDate).toLocaleDateString()} - {new Date(reservation.endDate).toLocaleDateString()}
                          </Typography>
                          <Chip label={`${reservation.totalDays} days`} size="small" variant="outlined" />
                        </Box>

                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                          <LocationOn fontSize="small" color="action" />
                          <Typography variant="body2">
                            Pickup: {reservation.pickupLocation}
                          </Typography>
                        </Box>

                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <DirectionsCar fontSize="small" color="action" />
                          <Typography variant="body2">
                            {reservation.company?.companyName}
                          </Typography>
                        </Box>
                      </Box>

                      {reservation.notes && (
                        <Box sx={{ mt: 2, p: 1, bgcolor: 'grey.100', borderRadius: 1 }}>
                          <Typography variant="caption" color="text.secondary">
                            Special Requests:
                          </Typography>
                          <Typography variant="body2">
                            {reservation.notes}
                          </Typography>
                        </Box>
                      )}
                    </Grid>

                    {/* Pricing & Actions */}
                    <Grid item xs={12} md={3}>
                      <Box sx={{ textAlign: 'right' }}>
                        <Box sx={{ mb: 2 }}>
                          <Typography variant="body2" color="text.secondary">
                            Total Amount
                          </Typography>
                          <Typography variant="h5" color="primary">
                            ${parseFloat(reservation.totalAmount.toString()).toFixed(2)}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            ${parseFloat(reservation.dailyRate.toString()).toFixed(2)}/day
                          </Typography>
                        </Box>

                        <Divider sx={{ my: 2 }} />

                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                          {reservation.vehicle?.id && (
                            <Button
                              variant="outlined"
                              size="small"
                              href={`/vehicles/${reservation.vehicle.id}`}
                            >
                              View Vehicle
                            </Button>
                          )}

                          {(reservation.status === ReservationStatus.PENDING || 
                            reservation.status === ReservationStatus.CONFIRMED) && (
                            <Button
                              variant="outlined"
                              color="error"
                              size="small"
                              startIcon={<CancelIcon />}
                              onClick={() => handleCancelClick(reservation)}
                            >
                              Cancel
                            </Button>
                          )}
                        </Box>
                      </Box>
                    </Grid>
                  </Grid>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Cancel Dialog */}
      <Dialog open={cancelDialogOpen} onClose={() => setCancelDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Cancel Reservation</DialogTitle>
        <DialogContent>
          <Typography variant="body2" gutterBottom>
            Are you sure you want to cancel this reservation?
          </Typography>
          {selectedReservation && (
            <Box sx={{ my: 2, p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
              <Typography variant="body2">
                <strong>Vehicle:</strong> {selectedReservation.vehicle?.make} {selectedReservation.vehicle?.model}
              </Typography>
              <Typography variant="body2">
                <strong>Dates:</strong> {new Date(selectedReservation.startDate).toLocaleDateString()} - {new Date(selectedReservation.endDate).toLocaleDateString()}
              </Typography>
            </Box>
          )}
          <TextField
            fullWidth
            multiline
            rows={3}
            label="Cancellation Reason (optional)"
            value={cancelReason}
            onChange={(e) => setCancelReason(e.target.value)}
            margin="normal"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialogOpen(false)}>Keep Reservation</Button>
          <Button
            onClick={handleConfirmCancel}
            color="error"
            variant="contained"
            disabled={cancelMutation.isLoading}
          >
            {cancelMutation.isLoading ? 'Cancelling...' : 'Confirm Cancellation'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};
