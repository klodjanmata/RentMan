import React, { useState } from 'react';
import {
  Box,
  Container,
  Grid,
  Paper,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Chip,
  Card,
  CardContent,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  CircularProgress,
} from '@mui/material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// API functions
const platformAdminApi = {
  getDashboard: async () => {
    const token = localStorage.getItem('token');
    const response = await axios.get(`${API_BASE_URL}/platform-admin/dashboard`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  },
  getPendingCompanies: async () => {
    const token = localStorage.getItem('token');
    const response = await axios.get(`${API_BASE_URL}/platform-admin/companies/pending`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  },
  approveCompany: async (companyId: number) => {
    const token = localStorage.getItem('token');
    const response = await axios.post(
      `${API_BASE_URL}/platform-admin/companies/${companyId}/approve`,
      {},
      { headers: { Authorization: `Bearer ${token}` } }
    );
    return response.data;
  },
  rejectCompany: async ({ companyId, reason }: { companyId: number; reason: string }) => {
    const token = localStorage.getItem('token');
    const response = await axios.post(
      `${API_BASE_URL}/platform-admin/companies/${companyId}/reject`,
      { reason },
      { headers: { Authorization: `Bearer ${token}` } }
    );
    return response.data;
  },
  suspendCompany: async ({ companyId, reason }: { companyId: number; reason: string }) => {
    const token = localStorage.getItem('token');
    const response = await axios.post(
      `${API_BASE_URL}/platform-admin/companies/${companyId}/suspend`,
      { reason },
      { headers: { Authorization: `Bearer ${token}` } }
    );
    return response.data;
  },
};

interface Company {
  id: number;
  companyName: string;
  email: string;
  phoneNumber: string;
  city: string;
  state: string;
  status: string;
  createdAt: string;
  subscriptionPlan: string;
  businessRegistrationNumber: string;
  taxId: string;
}

export const PlatformAdminDashboardPage: React.FC = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [selectedCompany, setSelectedCompany] = useState<Company | null>(null);
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [suspendDialogOpen, setSuspendDialogOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [suspendReason, setSuspendReason] = useState('');
  const [actionMessage, setActionMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Redirect non-admin users
  React.useEffect(() => {
    if (user && user.role !== 'ADMIN') {
      if (user.role === 'COMPANY_ADMIN' || user.role === 'EMPLOYEE') {
        navigate('/company/dashboard');
      } else {
        navigate('/dashboard');
      }
    }
  }, [user, navigate]);

  // Fetch dashboard data
  const { data: dashboardData, isLoading: dashboardLoading } = useQuery(
    'platformDashboard',
    platformAdminApi.getDashboard
  );

  // Fetch pending companies
  const { data: pendingCompanies, isLoading: pendingLoading } = useQuery(
    'pendingCompanies',
    platformAdminApi.getPendingCompanies
  );

  // Approve company mutation
  const approveMutation = useMutation(platformAdminApi.approveCompany, {
    onSuccess: () => {
      queryClient.invalidateQueries('pendingCompanies');
      queryClient.invalidateQueries('platformDashboard');
      setActionMessage({ type: 'success', text: 'Company approved successfully!' });
      setTimeout(() => setActionMessage(null), 5000);
    },
    onError: (error: any) => {
      setActionMessage({ 
        type: 'error', 
        text: error.response?.data?.error || 'Failed to approve company' 
      });
      setTimeout(() => setActionMessage(null), 5000);
    },
  });

  // Reject company mutation
  const rejectMutation = useMutation(platformAdminApi.rejectCompany, {
    onSuccess: () => {
      queryClient.invalidateQueries('pendingCompanies');
      queryClient.invalidateQueries('platformDashboard');
      setRejectDialogOpen(false);
      setRejectReason('');
      setSelectedCompany(null);
      setActionMessage({ type: 'success', text: 'Company rejected successfully!' });
      setTimeout(() => setActionMessage(null), 5000);
    },
    onError: (error: any) => {
      setActionMessage({ 
        type: 'error', 
        text: error.response?.data?.error || 'Failed to reject company' 
      });
      setTimeout(() => setActionMessage(null), 5000);
    },
  });

  // Suspend company mutation
  const suspendMutation = useMutation(platformAdminApi.suspendCompany, {
    onSuccess: () => {
      queryClient.invalidateQueries('pendingCompanies');
      queryClient.invalidateQueries('platformDashboard');
      setSuspendDialogOpen(false);
      setSuspendReason('');
      setSelectedCompany(null);
      setActionMessage({ type: 'success', text: 'Company suspended successfully!' });
      setTimeout(() => setActionMessage(null), 5000);
    },
    onError: (error: any) => {
      setActionMessage({ 
        type: 'error', 
        text: error.response?.data?.error || 'Failed to suspend company' 
      });
      setTimeout(() => setActionMessage(null), 5000);
    },
  });

  const handleApprove = (company: Company) => {
    if (window.confirm(`Are you sure you want to approve ${company.companyName}?`)) {
      approveMutation.mutate(company.id);
    }
  };

  const handleReject = (company: Company) => {
    setSelectedCompany(company);
    setRejectDialogOpen(true);
  };

  const handleSuspend = (company: Company) => {
    setSelectedCompany(company);
    setSuspendDialogOpen(true);
  };

  const confirmReject = () => {
    if (selectedCompany) {
      rejectMutation.mutate({ companyId: selectedCompany.id, reason: rejectReason });
    }
  };

  const confirmSuspend = () => {
    if (selectedCompany) {
      suspendMutation.mutate({ companyId: selectedCompany.id, reason: suspendReason });
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'PENDING_APPROVAL':
        return 'warning';
      case 'SUSPENDED':
        return 'error';
      case 'REJECTED':
        return 'error';
      case 'INACTIVE':
        return 'default';
      default:
        return 'default';
    }
  };

  if (dashboardLoading || pendingLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Platform Administration Dashboard
      </Typography>

      {actionMessage && (
        <Alert severity={actionMessage.type} sx={{ mb: 3 }} onClose={() => setActionMessage(null)}>
          {actionMessage.text}
        </Alert>
      )}

      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Total Companies
              </Typography>
              <Typography variant="h4">{dashboardData?.totalCompanies || 0}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Pending Approvals
              </Typography>
              <Typography variant="h4" color="warning.main">
                {dashboardData?.pendingApprovals || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Active Companies
              </Typography>
              <Typography variant="h4" color="success.main">
                {dashboardData?.companyStatistics?.ACTIVE || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Total Users
              </Typography>
              <Typography variant="h4">{dashboardData?.totalUsers || 0}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Pending Companies Table */}
      <Paper sx={{ p: 3 }}>
        <Typography variant="h5" gutterBottom>
          Pending Company Approvals
        </Typography>
        {pendingCompanies && pendingCompanies.length > 0 ? (
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Company Name</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell>Location</TableCell>
                  <TableCell>Registration #</TableCell>
                  <TableCell>Created At</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {pendingCompanies.map((company: Company) => (
                  <TableRow key={company.id}>
                    <TableCell>{company.companyName}</TableCell>
                    <TableCell>{company.email}</TableCell>
                    <TableCell>
                      {company.city}, {company.state}
                    </TableCell>
                    <TableCell>{company.businessRegistrationNumber}</TableCell>
                    <TableCell>{new Date(company.createdAt).toLocaleDateString()}</TableCell>
                    <TableCell>
                      <Chip
                        label={company.status.replace('_', ' ')}
                        color={getStatusColor(company.status) as any}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Button
                        variant="contained"
                        color="success"
                        size="small"
                        onClick={() => handleApprove(company)}
                        sx={{ mr: 1 }}
                        disabled={approveMutation.isLoading}
                      >
                        Approve
                      </Button>
                      <Button
                        variant="outlined"
                        color="error"
                        size="small"
                        onClick={() => handleReject(company)}
                        disabled={rejectMutation.isLoading}
                      >
                        Reject
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        ) : (
          <Typography color="textSecondary" sx={{ py: 3, textAlign: 'center' }}>
            No pending company approvals
          </Typography>
        )}
      </Paper>

      {/* Reject Dialog */}
      <Dialog open={rejectDialogOpen} onClose={() => setRejectDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Reject Company</DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mb: 2 }}>
            Are you sure you want to reject <strong>{selectedCompany?.companyName}</strong>?
          </Typography>
          <TextField
            fullWidth
            multiline
            rows={4}
            label="Rejection Reason"
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
            placeholder="Please provide a reason for rejection..."
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRejectDialogOpen(false)}>Cancel</Button>
          <Button onClick={confirmReject} color="error" variant="contained" disabled={!rejectReason.trim()}>
            Reject Company
          </Button>
        </DialogActions>
      </Dialog>

      {/* Suspend Dialog */}
      <Dialog open={suspendDialogOpen} onClose={() => setSuspendDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Suspend Company</DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mb: 2 }}>
            Are you sure you want to suspend <strong>{selectedCompany?.companyName}</strong>?
          </Typography>
          <TextField
            fullWidth
            multiline
            rows={4}
            label="Suspension Reason"
            value={suspendReason}
            onChange={(e) => setSuspendReason(e.target.value)}
            placeholder="Please provide a reason for suspension..."
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSuspendDialogOpen(false)}>Cancel</Button>
          <Button onClick={confirmSuspend} color="error" variant="contained" disabled={!suspendReason.trim()}>
            Suspend Company
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

