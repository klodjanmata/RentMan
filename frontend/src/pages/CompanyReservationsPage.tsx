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
  Alert,
  CircularProgress,
  Tab,
  Tabs,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from '@mui/material';
import {
  CheckCircle,
  Cancel,
  Pending,
  HourglassEmpty,
  DirectionsCar,
  Person,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import axios from 'axios';
import { useAuth } from '../contexts/AuthContext';
import { ReservationStatus } from '../types/reservation';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

interface Reservation {
  id: number;
  reservationNumber: string;
  vehicle: any;
  customer: any;
  company: any;
  startDate: string;
  endDate: string;
  totalDays: number;
  dailyRate: number;
  totalAmount: number;
  status: string;
  pickupLocation: string;
  returnLocation: string;
  specialRequests: string;
  createdAt: string;
}

// API functions
const companyReservationsApi = {
  getCompanyReservations: async (companyId: number) => {
    const token = localStorage.getItem('token');
    const response = await axios.get(`${API_BASE_URL}/companies/${companyId}/reservations`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  },
  confirmReservation: async (reservationId: number) => {
    const token = localStorage.getItem('token');
    const response = await axios.patch(
      `${API_BASE_URL}/reservations/${reservationId}/confirm`,
      {},
      { headers: { Authorization: `Bearer ${token}` } }
    );
    return response.data;
  },
  cancelReservation: async (reservationId: number) => {
    const token = localStorage.getItem('token');
    const response = await axios.patch(
      `${API_BASE_URL}/reservations/${reservationId}/cancel`,
      {},
      { headers: { Authorization: `Bearer ${token}` } }
    );
    return response.data;
  },
};

export const CompanyReservationsPage: React.FC = () => {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [selectedTab, setSelectedTab] = useState(0);
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [selectedReservation, setSelectedReservation] = useState<Reservation | null>(null);

  const companyId = user?.company?.id;

  // Fetch company reservations
  const { data: reservations, isLoading, error } = useQuery(
    ['companyReservations', companyId],
    () => companyReservationsApi.getCompanyReservations(companyId!),
    { enabled: !!companyId }
  );

  // Confirm reservation mutation
  const confirmMutation = useMutation(companyReservationsApi.confirmReservation, {
    onSuccess: () => {
      queryClient.invalidateQueries('companyReservations');
      setConfirmDialogOpen(false);
      setSelectedReservation(null);
    },
  });

  const handleConfirmClick = (reservation: Reservation) => {
    setSelectedReservation(reservation);
    setConfirmDialogOpen(true);
  };

  const handleConfirm = () => {
    if (selectedReservation) {
      confirmMutation.mutate(selectedReservation.id);
    }
  };

  const getStatusColor = (status: string): "default" | "primary" | "secondary" | "error" | "info" | "success" | "warning" => {
    switch (status) {
      case 'CONFIRMED':
        return 'success';
      case 'PENDING':
        return 'warning';
      case 'IN_PROGRESS':
        return 'info';
      case 'COMPLETED':
        return 'primary';
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
    }
  };

  const filterReservations = (reservations: Reservation[]) => {
    if (!reservations) return [];
    switch (selectedTab) {
      case 0: // All
        return reservations;
      case 1: // Pending
        return reservations.filter(r => r.status === 'PENDING');
      case 2: // Confirmed
        return reservations.filter(r => r.status === 'CONFIRMED');
      case 3: // In Progress
        return reservations.filter(r => r.status === 'IN_PROGRESS');
      case 4: // Completed
        return reservations.filter(r => r.status === 'COMPLETED');
      default:
        return reservations;
    }
  };

  if (!companyId) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error">You must be associated with a company to view reservations.</Alert>
      </Container>
    );
  }

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

  const filteredReservations = filterReservations(reservations);
  const pendingCount = reservations?.filter((r: Reservation) => r.status === 'PENDING').length || 0;

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Reservations Management
        </Typography>
        {pendingCount > 0 && (
          <Chip label={`${pendingCount} Pending Approvals`} color="warning" />
        )}
      </Box>

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs value={selectedTab} onChange={(e, newValue) => setSelectedTab(newValue)}>
          <Tab label={`All (${reservations?.length || 0})`} />
          <Tab label={`Pending (${pendingCount})`} />
          <Tab label="Confirmed" />
          <Tab label="In Progress" />
          <Tab label="Completed" />
        </Tabs>
      </Box>

      {/* Reservations Table */}
      {filteredReservations.length === 0 ? (
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <Typography variant="h6" color="text.secondary">
            No reservations found
          </Typography>
        </Box>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Reservation #</TableCell>
                <TableCell>Customer</TableCell>
                <TableCell>Vehicle</TableCell>
                <TableCell>Dates</TableCell>
                <TableCell>Duration</TableCell>
                <TableCell>Amount</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredReservations.map((reservation: Reservation) => (
                <TableRow key={reservation.id}>
                  <TableCell>
                    <Typography variant="body2" fontWeight="bold">
                      {reservation.reservationNumber}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2">
                      {reservation.customer?.firstName} {reservation.customer?.lastName}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {reservation.customer?.email}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2">
                      {reservation.vehicle?.make} {reservation.vehicle?.model}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {reservation.vehicle?.year}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2">
                      {new Date(reservation.startDate).toLocaleDateString()}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      to {new Date(reservation.endDate).toLocaleDateString()}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip label={`${reservation.totalDays} days`} size="small" />
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" fontWeight="bold">
                      ${parseFloat(reservation.totalAmount?.toString() || '0').toFixed(2)}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={reservation.status}
                      color={getStatusColor(reservation.status)}
                      size="small"
                    />
                  </TableCell>
                  <TableCell align="right">
                    {reservation.status === 'PENDING' && (
                      <Button
                        variant="contained"
                        color="success"
                        size="small"
                        onClick={() => handleConfirmClick(reservation)}
                        disabled={confirmMutation.isLoading}
                      >
                        Confirm
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Confirm Dialog */}
      <Dialog open={confirmDialogOpen} onClose={() => setConfirmDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Confirm Reservation</DialogTitle>
        <DialogContent>
          <Typography variant="body2" gutterBottom>
            Are you sure you want to confirm this reservation?
          </Typography>
          {selectedReservation && (
            <Box sx={{ my: 2, p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
              <Typography variant="body2">
                <strong>Reservation:</strong> {selectedReservation.reservationNumber}
              </Typography>
              <Typography variant="body2">
                <strong>Customer:</strong> {selectedReservation.customer?.firstName} {selectedReservation.customer?.lastName}
              </Typography>
              <Typography variant="body2">
                <strong>Vehicle:</strong> {selectedReservation.vehicle?.make} {selectedReservation.vehicle?.model}
              </Typography>
              <Typography variant="body2">
                <strong>Dates:</strong> {new Date(selectedReservation.startDate).toLocaleDateString()} - {new Date(selectedReservation.endDate).toLocaleDateString()}
              </Typography>
              <Typography variant="body2">
                <strong>Amount:</strong> ${parseFloat(selectedReservation.totalAmount?.toString() || '0').toFixed(2)}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDialogOpen(false)}>Cancel</Button>
          <Button
            onClick={handleConfirm}
            color="success"
            variant="contained"
            disabled={confirmMutation.isLoading}
          >
            {confirmMutation.isLoading ? 'Confirming...' : 'Confirm Reservation'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

