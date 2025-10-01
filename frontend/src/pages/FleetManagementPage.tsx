import React, { useState, useEffect } from 'react';
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
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Switch,
  FormControlLabel,
  Alert,
  Snackbar,
  Fab,
  Tooltip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  InputAdornment,
  Menu,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import {
  DirectionsCar,
  Add,
  Edit,
  Delete,
  Visibility,
  MoreVert,
  Search,
  FilterList,
  Refresh,
  CarRepair,
  LocalGasStation,
  Speed,
  People,
  CalendarToday,
  AttachMoney,
  CheckCircle,
  Warning,
  Error,
  Info,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { Vehicle, VehicleType, VehicleStatus } from '../types/vehicle';

interface VehicleFormData {
  make: string;
  model: string;
  year: number;
  licensePlate: string;
  type: VehicleType;
  status: VehicleStatus;
  dailyRate: number;
  fuelType: string;
  transmission: string;
  seatingCapacity: number;
  color: string;
  mileage: number;
  description: string;
  imageUrl: string;
  currentLocation: string;
  pickupLocation: string;
  isFeatured: boolean;
  isAvailableForRental: boolean;
  engineSize: string;
  fuelCapacity: number;
  doors: number;
  luggageCapacity: string;
  airConditioning: boolean;
  gpsNavigation: boolean;
  bluetooth: boolean;
  usbCharging: boolean;
  backupCamera: boolean;
  parkingSensors: boolean;
  sunroof: boolean;
  leatherSeats: boolean;
}

export const FleetManagementPage: React.FC = () => {
  const { user } = useAuth();
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  
  // Dialog states
  const [addDialogOpen, setAddDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [viewDialogOpen, setViewDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedVehicle, setSelectedVehicle] = useState<Vehicle | null>(null);
  
  // Form state
  const [formData, setFormData] = useState<VehicleFormData>({
    make: '',
    model: '',
    year: new Date().getFullYear(),
    licensePlate: '',
    type: VehicleType.CAR,
    status: VehicleStatus.AVAILABLE,
    dailyRate: 0,
    fuelType: 'Gasoline',
    transmission: 'Automatic',
    seatingCapacity: 5,
    color: '',
    mileage: 0,
    description: '',
    imageUrl: '',
    currentLocation: '',
    pickupLocation: '',
    isFeatured: false,
    isAvailableForRental: true,
    engineSize: '',
    fuelCapacity: 0,
    doors: 4,
    luggageCapacity: '',
    airConditioning: true,
    gpsNavigation: false,
    bluetooth: false,
    usbCharging: false,
    backupCamera: false,
    parkingSensors: false,
    sunroof: false,
    leatherSeats: false,
  });
  
  // Table states
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState<VehicleStatus | 'ALL'>('ALL');
  const [filterType, setFilterType] = useState<VehicleType | 'ALL'>('ALL');
  
  // Menu states
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [menuVehicle, setMenuVehicle] = useState<Vehicle | null>(null);

  // Load vehicles
  const loadVehicles = async () => {
    try {
      setLoading(true);
      // Get all vehicles (for now, we'll filter by company on the frontend)
      // TODO: Implement company-specific vehicle endpoint
      const response = await fetch('http://localhost:8080/api/vehicles', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });
      
      if (!response.ok) {
        throw `HTTP error! status: ${response.status}`;
      }
      
      const vehiclesData = await response.json();
      setVehicles(vehiclesData);
    } catch (err: any) {
      console.error('Error loading vehicles:', err);
      setError('Failed to load vehicles: ' + err.message);
      // Fallback to empty array if API fails
      setVehicles([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadVehicles();
  }, []);

  // Filter vehicles
  const filteredVehicles = vehicles.filter(vehicle => {
    const matchesSearch = 
      vehicle.make.toLowerCase().includes(searchTerm.toLowerCase()) ||
      vehicle.model.toLowerCase().includes(searchTerm.toLowerCase()) ||
      vehicle.licensePlate?.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesStatus = filterStatus === 'ALL' || vehicle.status === filterStatus;
    const matchesType = filterType === 'ALL' || vehicle.type === filterType;
    
    return matchesSearch && matchesStatus && matchesType;
  });

  // Handle form input changes
  const handleInputChange = (field: keyof VehicleFormData, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  // Handle add vehicle
  const handleAddVehicle = async () => {
    try {
      // Add company ID to the form data
      const vehicleData = {
        ...formData,
        companyId: user?.company?.id
      };
      
      console.log('User object:', user);
      console.log('User company:', user?.company);
      console.log('Company ID:', user?.company?.id);
      console.log('Sending vehicle data:', vehicleData);
      
      const response = await fetch('http://localhost:8080/api/vehicles', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify(vehicleData),
      });
      
      console.log('Response status:', response.status);
      console.log('Response headers:', response.headers);
      
      if (!response.ok) {
        let errorMessage = `HTTP error! status: ${response.status}`;
        try {
          const errorData = await response.json();
          errorMessage = errorData.error || errorMessage;
        } catch (parseError) {
          // If response is not JSON, use the status text or default message
          errorMessage = response.statusText || errorMessage;
        }
        throw errorMessage;
      }
      
      const result = await response.json();
      console.log('Vehicle added successfully:', result);
      
      setSuccess('Vehicle added successfully!');
      setAddDialogOpen(false);
      resetForm();
      loadVehicles();
    } catch (err: any) {
      console.error('Error adding vehicle:', err);
      setError('Failed to add vehicle: ' + err);
    }
  };

  // Handle edit vehicle
  const handleEditVehicle = async () => {
    try {
      // Add company ID to the form data
      const vehicleData = {
        ...formData,
        companyId: user?.company?.id
      };
      
      const response = await fetch(`http://localhost:8080/api/vehicles/${selectedVehicle?.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify(vehicleData),
      });
      
      if (!response.ok) {
        let errorMessage = `HTTP error! status: ${response.status}`;
        try {
          const errorData = await response.json();
          errorMessage = errorData.error || errorMessage;
        } catch (parseError) {
          // If response is not JSON, use the status text or default message
          errorMessage = response.statusText || errorMessage;
        }
        throw errorMessage;
      }
      
      setSuccess('Vehicle updated successfully!');
      setEditDialogOpen(false);
      resetForm();
      loadVehicles();
    } catch (err: any) {
      setError('Failed to update vehicle: ' + err.message);
    }
  };

  // Handle delete vehicle
  const handleDeleteVehicle = async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/vehicles/${selectedVehicle?.id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });
      
      if (!response.ok) {
        let errorMessage = `HTTP error! status: ${response.status}`;
        try {
          const errorData = await response.json();
          errorMessage = errorData.error || errorMessage;
        } catch (parseError) {
          // If response is not JSON, use the status text or default message
          errorMessage = response.statusText || errorMessage;
        }
        throw errorMessage;
      }
      
      setSuccess('Vehicle deleted successfully!');
      setDeleteDialogOpen(false);
      loadVehicles();
    } catch (err: any) {
      setError('Failed to delete vehicle: ' + err.message);
    }
  };

  // Reset form
  const resetForm = () => {
    setFormData({
      make: '',
      model: '',
      year: new Date().getFullYear(),
      licensePlate: '',
      type: VehicleType.CAR,
      status: VehicleStatus.AVAILABLE,
      dailyRate: 0,
      fuelType: 'Gasoline',
      transmission: 'Automatic',
      seatingCapacity: 5,
      color: '',
      mileage: 0,
      description: '',
      imageUrl: '',
      currentLocation: '',
      pickupLocation: '',
      isFeatured: false,
      isAvailableForRental: true,
      engineSize: '',
      fuelCapacity: 0,
      doors: 4,
      luggageCapacity: '',
      airConditioning: true,
      gpsNavigation: false,
      bluetooth: false,
      usbCharging: false,
      backupCamera: false,
      parkingSensors: false,
      sunroof: false,
      leatherSeats: false,
    });
  };

  // Open edit dialog
  const openEditDialog = (vehicle: Vehicle) => {
    setSelectedVehicle(vehicle);
    setFormData({
      make: vehicle.make,
      model: vehicle.model,
      year: vehicle.year,
      licensePlate: vehicle.licensePlate || '',
      type: vehicle.type,
      status: vehicle.status,
      dailyRate: typeof vehicle.dailyRate === 'string' ? parseFloat(vehicle.dailyRate) : vehicle.dailyRate,
      fuelType: vehicle.fuelType || 'Gasoline',
      transmission: vehicle.transmission || 'Automatic',
      seatingCapacity: vehicle.seatingCapacity || 5,
      color: vehicle.color || '',
      mileage: vehicle.mileage || 0,
      description: vehicle.description || '',
      imageUrl: vehicle.imageUrls?.[0] || '',
      currentLocation: vehicle.currentLocation || '',
      pickupLocation: vehicle.pickupLocation || '',
      isFeatured: vehicle.isFeatured || false,
      isAvailableForRental: vehicle.isAvailableForRental || true,
      engineSize: '',
      fuelCapacity: 0,
      doors: 4,
      luggageCapacity: '',
      airConditioning: true,
      gpsNavigation: false,
      bluetooth: false,
      usbCharging: false,
      backupCamera: false,
      parkingSensors: false,
      sunroof: false,
      leatherSeats: false,
    });
    setEditDialogOpen(true);
  };

  // Get status color
  const getStatusColor = (status: VehicleStatus) => {
    switch (status) {
      case VehicleStatus.AVAILABLE:
        return 'success';
      case VehicleStatus.RENTED:
        return 'info';
      case VehicleStatus.MAINTENANCE:
        return 'warning';
      case VehicleStatus.OUT_OF_SERVICE:
        return 'error';
      default:
        return 'default';
    }
  };

  // Get status icon
  const getStatusIcon = (status: VehicleStatus) => {
    switch (status) {
      case VehicleStatus.AVAILABLE:
        return <CheckCircle />;
      case VehicleStatus.RENTED:
        return <Info />;
      case VehicleStatus.MAINTENANCE:
        return <Warning />;
      case VehicleStatus.OUT_OF_SERVICE:
        return <Error />;
      default:
        return <Info />;
    }
  };

  return (
    <Box>
      {/* Header */}
      <Box
        sx={{
          background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
          color: 'white',
          py: 4,
        }}
      >
        <Container maxWidth="lg">
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Avatar
                sx={{
                  width: 64,
                  height: 64,
                  mr: 3,
                  bgcolor: 'rgba(255,255,255,0.2)',
                  fontSize: '1.5rem',
                }}
              >
                <DirectionsCar />
              </Avatar>
              <Box>
                <Typography variant="h4" component="h1" gutterBottom>
                  Fleet Management
                </Typography>
                <Typography variant="h6" sx={{ opacity: 0.9 }}>
                  Manage your vehicle fleet
                </Typography>
              </Box>
            </Box>
            <Button
              variant="contained"
              startIcon={<Add />}
              onClick={() => setAddDialogOpen(true)}
              sx={{
                bgcolor: 'rgba(255,255,255,0.2)',
                color: 'white',
                '&:hover': { bgcolor: 'rgba(255,255,255,0.3)' },
              }}
            >
              Add Vehicle
            </Button>
          </Box>
        </Container>
      </Box>

      <Container maxWidth="lg" sx={{ py: 4 }}>
        {/* Filters and Search */}
        <Paper elevation={2} sx={{ p: 3, mb: 3 }}>
          <Grid container spacing={3} alignItems="center">
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                placeholder="Search vehicles..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Search />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>Status</InputLabel>
                <Select
                  value={filterStatus}
                  onChange={(e) => setFilterStatus(e.target.value as VehicleStatus | 'ALL')}
                  label="Status"
                >
                  <MenuItem value="ALL">All Statuses</MenuItem>
                  <MenuItem value={VehicleStatus.AVAILABLE}>Available</MenuItem>
                  <MenuItem value={VehicleStatus.RENTED}>Rented</MenuItem>
                  <MenuItem value={VehicleStatus.MAINTENANCE}>Maintenance</MenuItem>
                  <MenuItem value={VehicleStatus.OUT_OF_SERVICE}>Out of Service</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>Type</InputLabel>
                <Select
                  value={filterType}
                  onChange={(e) => setFilterType(e.target.value as VehicleType | 'ALL')}
                  label="Type"
                >
                  <MenuItem value="ALL">All Types</MenuItem>
                  <MenuItem value={VehicleType.CAR}>Car</MenuItem>
                  <MenuItem value={VehicleType.SUV}>SUV</MenuItem>
                  <MenuItem value={VehicleType.TRUCK}>Truck</MenuItem>
                  <MenuItem value={VehicleType.VAN}>Van</MenuItem>
                  <MenuItem value={VehicleType.MOTORCYCLE}>Motorcycle</MenuItem>
                  <MenuItem value={VehicleType.LUXURY}>Luxury</MenuItem>
                  <MenuItem value={VehicleType.CONVERTIBLE}>Convertible</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={2}>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<Refresh />}
                onClick={loadVehicles}
              >
                Refresh
              </Button>
            </Grid>
          </Grid>
        </Paper>

        {/* Vehicles Table */}
        <Paper elevation={2}>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Vehicle</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Daily Rate</TableCell>
                  <TableCell>Mileage</TableCell>
                  <TableCell>Location</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredVehicles
                  .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                  .map((vehicle) => (
                    <TableRow key={vehicle.id} hover>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                          <Avatar
                            sx={{
                              width: 48,
                              height: 48,
                              mr: 2,
                              bgcolor: 'primary.light',
                            }}
                          >
                            <DirectionsCar />
                          </Avatar>
                          <Box>
                            <Typography variant="subtitle1" fontWeight="bold">
                              {vehicle.make} {vehicle.model}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              {vehicle.year} • {vehicle.licensePlate}
                            </Typography>
                          </Box>
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={vehicle.type}
                          size="small"
                          variant="outlined"
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          icon={getStatusIcon(vehicle.status)}
                          label={vehicle.status}
                          color={getStatusColor(vehicle.status) as any}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" fontWeight="bold">
                          ${(typeof vehicle.dailyRate === 'string' ? parseFloat(vehicle.dailyRate) : vehicle.dailyRate).toFixed(2)}/day
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {vehicle.mileage?.toLocaleString()} mi
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {vehicle.currentLocation || 'Not specified'}
                        </Typography>
                      </TableCell>
                      <TableCell align="right">
                        <IconButton
                          onClick={(e) => {
                            setAnchorEl(e.currentTarget);
                            setMenuVehicle(vehicle);
                          }}
                        >
                          <MoreVert />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={filteredVehicles.length}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={(_, newPage) => setPage(newPage)}
            onRowsPerPageChange={(e) => {
              setRowsPerPage(parseInt(e.target.value, 10));
              setPage(0);
            }}
          />
        </Paper>

        {/* Action Menu */}
        <Menu
          anchorEl={anchorEl}
          open={Boolean(anchorEl)}
          onClose={() => setAnchorEl(null)}
        >
          <MenuItem
            onClick={() => {
              setSelectedVehicle(menuVehicle);
              setViewDialogOpen(true);
              setAnchorEl(null);
            }}
          >
            <ListItemIcon>
              <Visibility />
            </ListItemIcon>
            <ListItemText>View Details</ListItemText>
          </MenuItem>
          <MenuItem
            onClick={() => {
              if (menuVehicle) openEditDialog(menuVehicle);
              setAnchorEl(null);
            }}
          >
            <ListItemIcon>
              <Edit />
            </ListItemIcon>
            <ListItemText>Edit</ListItemText>
          </MenuItem>
          <MenuItem
            onClick={() => {
              setSelectedVehicle(menuVehicle);
              setDeleteDialogOpen(true);
              setAnchorEl(null);
            }}
            sx={{ color: 'error.main' }}
          >
            <ListItemIcon>
              <Delete color="error" />
            </ListItemIcon>
            <ListItemText>Delete</ListItemText>
          </MenuItem>
        </Menu>

        {/* Add Vehicle Dialog */}
        <Dialog open={addDialogOpen} onClose={() => setAddDialogOpen(false)} maxWidth="md" fullWidth>
          <DialogTitle>Add New Vehicle</DialogTitle>
          <DialogContent>
            <VehicleForm formData={formData} onInputChange={handleInputChange} />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setAddDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleAddVehicle} variant="contained">
              Add Vehicle
            </Button>
          </DialogActions>
        </Dialog>

        {/* Edit Vehicle Dialog */}
        <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="md" fullWidth>
          <DialogTitle>Edit Vehicle</DialogTitle>
          <DialogContent>
            <VehicleForm formData={formData} onInputChange={handleInputChange} />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setEditDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleEditVehicle} variant="contained">
              Update Vehicle
            </Button>
          </DialogActions>
        </Dialog>

        {/* View Vehicle Dialog */}
        <Dialog open={viewDialogOpen} onClose={() => setViewDialogOpen(false)} maxWidth="md" fullWidth>
          <DialogTitle>Vehicle Details</DialogTitle>
          <DialogContent>
            {selectedVehicle && <VehicleDetails vehicle={selectedVehicle} />}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setViewDialogOpen(false)}>Close</Button>
          </DialogActions>
        </Dialog>

        {/* Delete Confirmation Dialog */}
        <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
          <DialogTitle>Delete Vehicle</DialogTitle>
          <DialogContent>
            <Typography>
              Are you sure you want to delete {selectedVehicle?.make} {selectedVehicle?.model}?
              This action cannot be undone.
            </Typography>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleDeleteVehicle} color="error" variant="contained">
              Delete
            </Button>
          </DialogActions>
        </Dialog>

        {/* Snackbars */}
        <Snackbar
          open={!!error}
          autoHideDuration={6000}
          onClose={() => setError(null)}
        >
          <Alert onClose={() => setError(null)} severity="error">
            {error}
          </Alert>
        </Snackbar>
        <Snackbar
          open={!!success}
          autoHideDuration={6000}
          onClose={() => setSuccess(null)}
        >
          <Alert onClose={() => setSuccess(null)} severity="success">
            {success}
          </Alert>
        </Snackbar>
      </Container>
    </Box>
  );
};

// Vehicle Form Component
const VehicleForm: React.FC<{
  formData: VehicleFormData;
  onInputChange: (field: keyof VehicleFormData, value: any) => void;
}> = ({ formData, onInputChange }) => {
  return (
    <Box sx={{ pt: 2 }}>
      <Grid container spacing={3}>
        {/* Basic Information */}
        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom>
            Basic Information
          </Typography>
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Make"
            value={formData.make}
            onChange={(e) => onInputChange('make', e.target.value)}
            required
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Model"
            value={formData.model}
            onChange={(e) => onInputChange('model', e.target.value)}
            required
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Year"
            type="number"
            value={formData.year}
            onChange={(e) => onInputChange('year', parseInt(e.target.value))}
            required
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="License Plate"
            value={formData.licensePlate}
            onChange={(e) => onInputChange('licensePlate', e.target.value)}
            required
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <FormControl fullWidth>
            <InputLabel>Type</InputLabel>
            <Select
              value={formData.type}
              onChange={(e) => onInputChange('type', e.target.value)}
              label="Type"
            >
              <MenuItem value={VehicleType.CAR}>Car</MenuItem>
              <MenuItem value={VehicleType.SUV}>SUV</MenuItem>
              <MenuItem value={VehicleType.TRUCK}>Truck</MenuItem>
              <MenuItem value={VehicleType.VAN}>Van</MenuItem>
              <MenuItem value={VehicleType.MOTORCYCLE}>Motorcycle</MenuItem>
              <MenuItem value={VehicleType.LUXURY}>Luxury</MenuItem>
              <MenuItem value={VehicleType.CONVERTIBLE}>Convertible</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        <Grid item xs={12} sm={6}>
          <FormControl fullWidth>
            <InputLabel>Status</InputLabel>
            <Select
              value={formData.status}
              onChange={(e) => onInputChange('status', e.target.value)}
              label="Status"
            >
              <MenuItem value={VehicleStatus.AVAILABLE}>Available</MenuItem>
              <MenuItem value={VehicleStatus.RENTED}>Rented</MenuItem>
              <MenuItem value={VehicleStatus.MAINTENANCE}>Maintenance</MenuItem>
              <MenuItem value={VehicleStatus.OUT_OF_SERVICE}>Out of Service</MenuItem>
            </Select>
          </FormControl>
        </Grid>

        {/* Pricing and Specifications */}
        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
            Pricing & Specifications
          </Typography>
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Daily Rate"
            type="number"
            value={formData.dailyRate}
            onChange={(e) => onInputChange('dailyRate', parseFloat(e.target.value))}
            InputProps={{
              startAdornment: <InputAdornment position="start">$</InputAdornment>,
            }}
            required
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Mileage"
            type="number"
            value={formData.mileage}
            onChange={(e) => onInputChange('mileage', parseInt(e.target.value))}
            InputProps={{
              endAdornment: <InputAdornment position="end">mi</InputAdornment>,
            }}
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Fuel Type"
            value={formData.fuelType}
            onChange={(e) => onInputChange('fuelType', e.target.value)}
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Transmission"
            value={formData.transmission}
            onChange={(e) => onInputChange('transmission', e.target.value)}
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Seating Capacity"
            type="number"
            value={formData.seatingCapacity}
            onChange={(e) => onInputChange('seatingCapacity', parseInt(e.target.value))}
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Color"
            value={formData.color}
            onChange={(e) => onInputChange('color', e.target.value)}
          />
        </Grid>

        {/* Location */}
        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
            Location
          </Typography>
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Current Location"
            value={formData.currentLocation}
            onChange={(e) => onInputChange('currentLocation', e.target.value)}
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Pickup Location"
            value={formData.pickupLocation}
            onChange={(e) => onInputChange('pickupLocation', e.target.value)}
          />
        </Grid>

        {/* Features */}
        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
            Features
          </Typography>
        </Grid>
        <Grid item xs={12} sm={6}>
          <FormControlLabel
            control={
              <Switch
                checked={formData.airConditioning}
                onChange={(e) => onInputChange('airConditioning', e.target.checked)}
              />
            }
            label="Air Conditioning"
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <FormControlLabel
            control={
              <Switch
                checked={formData.gpsNavigation}
                onChange={(e) => onInputChange('gpsNavigation', e.target.checked)}
              />
            }
            label="GPS Navigation"
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <FormControlLabel
            control={
              <Switch
                checked={formData.bluetooth}
                onChange={(e) => onInputChange('bluetooth', e.target.checked)}
              />
            }
            label="Bluetooth"
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <FormControlLabel
            control={
              <Switch
                checked={formData.backupCamera}
                onChange={(e) => onInputChange('backupCamera', e.target.checked)}
              />
            }
            label="Backup Camera"
          />
        </Grid>

        {/* Description */}
        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Description"
            multiline
            rows={3}
            value={formData.description}
            onChange={(e) => onInputChange('description', e.target.value)}
          />
        </Grid>

        {/* Image URL */}
        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Image URL"
            value={formData.imageUrl}
            onChange={(e) => onInputChange('imageUrl', e.target.value)}
          />
        </Grid>
      </Grid>
    </Box>
  );
};

// Vehicle Details Component
const VehicleDetails: React.FC<{ vehicle: Vehicle }> = ({ vehicle }) => {
  return (
    <Box>
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Typography variant="h6" gutterBottom>
            Basic Information
          </Typography>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Make & Model
            </Typography>
            <Typography variant="body1">
              {vehicle.make} {vehicle.model}
            </Typography>
          </Box>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Year
            </Typography>
            <Typography variant="body1">{vehicle.year}</Typography>
          </Box>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="text.secondary">
              License Plate
            </Typography>
            <Typography variant="body1">{vehicle.licensePlate}</Typography>
          </Box>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Type
            </Typography>
            <Typography variant="body1">{vehicle.type}</Typography>
          </Box>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Status
            </Typography>
            <Chip
              label={vehicle.status}
              color="success"
              size="small"
            />
          </Box>
        </Grid>
        <Grid item xs={12} md={6}>
          <Typography variant="h6" gutterBottom>
            Pricing & Specifications
          </Typography>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Daily Rate
            </Typography>
            <Typography variant="body1" fontWeight="bold">
              ${(typeof vehicle.dailyRate === 'string' ? parseFloat(vehicle.dailyRate) : vehicle.dailyRate).toFixed(2)}/day
            </Typography>
          </Box>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Mileage
            </Typography>
            <Typography variant="body1">
              {vehicle.mileage?.toLocaleString()} miles
            </Typography>
          </Box>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Fuel Type
            </Typography>
            <Typography variant="body1">{vehicle.fuelType}</Typography>
          </Box>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Transmission
            </Typography>
            <Typography variant="body1">{vehicle.transmission}</Typography>
          </Box>
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Seating Capacity
            </Typography>
            <Typography variant="body1">{vehicle.seatingCapacity} passengers</Typography>
          </Box>
        </Grid>
        {vehicle.description && (
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>
              Description
            </Typography>
            <Typography variant="body1">{vehicle.description}</Typography>
          </Grid>
        )}
      </Grid>
    </Box>
  );
};
