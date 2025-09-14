import React, { useState } from 'react';
import {
  Box,
  Container,
  Grid,
  Paper,
  TextField,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Card,
  CardContent,
  CardMedia,
  CardActions,
  Typography,
  Chip,
  FormControlLabel,
  Checkbox,
  Pagination,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { useQuery } from 'react-query';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import { vehicleApi } from '../services/api';
import { Vehicle, VehicleType, VehicleSearchParams } from '../types/vehicle';
import { companyApi } from '../services/api';

export const SearchPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useState<VehicleSearchParams>({});
  const [page, setPage] = useState(1);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);

  const { data: companies } = useQuery('companies', companyApi.getActive);

  const { data: searchResults, isLoading, error } = useQuery(
    ['searchVehicles', searchParams, page],
    () => vehicleApi.search(searchParams),
    {
      onSuccess: (data) => {
        setVehicles(Array.isArray(data) ? data : []);
      },
      onError: (error) => {
        console.error('Search error:', error);
        setVehicles([]);
      },
    }
  );

  const handleSearch = () => {
    setPage(1);
    // Trigger search by updating searchParams
  };

  const handleVehicleClick = (vehicleId: number) => {
    navigate(`/vehicles/${vehicleId}`);
  };

  const handleReserveClick = (vehicleId: number) => {
    navigate(`/vehicles/${vehicleId}?action=reserve`);
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <Container maxWidth="lg" sx={{ py: 3 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Search Vehicles
        </Typography>

        {/* Search Filters */}
        <Paper sx={{ p: 3, mb: 3 }}>
          <Grid container spacing={3}>
            <Grid item xs={12} sm={6} md={3}>
              <FormControl fullWidth>
                <InputLabel>Company</InputLabel>
                <Select
                  value={searchParams.companyId || ''}
                  onChange={(e) =>
                    setSearchParams({
                      ...searchParams,
                      companyId: e.target.value ? Number(e.target.value) : undefined,
                    })
                  }
                >
                  <MenuItem value="">All Companies</MenuItem>
                  {companies?.map((company) => (
                    <MenuItem key={company.id} value={company.id}>
                      {company.companyName}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <FormControl fullWidth>
                <InputLabel>Vehicle Type</InputLabel>
                <Select
                  value={searchParams.type || ''}
                  onChange={(e) =>
                    setSearchParams({
                      ...searchParams,
                      type: e.target.value as VehicleType,
                    })
                  }
                >
                  <MenuItem value="">All Types</MenuItem>
                  {Object.values(VehicleType).map((type) => (
                    <MenuItem key={type} value={type}>
                      {type.replace('_', ' ')}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <TextField
                fullWidth
                label="Make"
                value={searchParams.make || ''}
                onChange={(e) =>
                  setSearchParams({ ...searchParams, make: e.target.value })
                }
              />
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <TextField
                fullWidth
                label="Model"
                value={searchParams.model || ''}
                onChange={(e) =>
                  setSearchParams({ ...searchParams, model: e.target.value })
                }
              />
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <TextField
                fullWidth
                label="Min Daily Rate"
                type="number"
                value={searchParams.minRate || ''}
                onChange={(e) =>
                  setSearchParams({
                    ...searchParams,
                    minRate: e.target.value ? Number(e.target.value) : undefined,
                  })
                }
              />
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <TextField
                fullWidth
                label="Max Daily Rate"
                type="number"
                value={searchParams.maxRate || ''}
                onChange={(e) =>
                  setSearchParams({
                    ...searchParams,
                    maxRate: e.target.value ? Number(e.target.value) : undefined,
                  })
                }
              />
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <DatePicker
                label="Start Date"
                value={searchParams.startDate ? dayjs(searchParams.startDate) : null}
                onChange={(date) =>
                  setSearchParams({
                    ...searchParams,
                    startDate: date ? date.format('YYYY-MM-DD') : undefined,
                  })
                }
                slotProps={{
                  textField: {
                    fullWidth: true,
                  },
                }}
              />
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <DatePicker
                label="End Date"
                value={searchParams.endDate ? dayjs(searchParams.endDate) : null}
                onChange={(date) =>
                  setSearchParams({
                    ...searchParams,
                    endDate: date ? date.format('YYYY-MM-DD') : undefined,
                  })
                }
                slotProps={{
                  textField: {
                    fullWidth: true,
                  },
                }}
              />
            </Grid>

            <Grid item xs={12}>
              <Button
                variant="contained"
                size="large"
                onClick={handleSearch}
                sx={{ mr: 2 }}
              >
                Search Vehicles
              </Button>
              <Button
                variant="outlined"
                onClick={() => setSearchParams({})}
              >
                Clear Filters
              </Button>
            </Grid>
          </Grid>
        </Paper>

        {/* Search Results */}
        {isLoading ? (
          <Typography>Loading vehicles...</Typography>
        ) : error ? (
          <Box sx={{ textAlign: 'center', py: 4 }}>
            <Typography variant="h6" color="error">
              Error loading vehicles: {error instanceof Error ? error.message : 'Unknown error occurred'}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Please check if the backend is running on http://localhost:8080
            </Typography>
          </Box>
        ) : (
          <Grid container spacing={3}>
            {(vehicles || []).map((vehicle) => (
              <Grid item xs={12} sm={6} md={4} key={vehicle.id}>
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
                  onClick={() => handleVehicleClick(vehicle.id)}
                >
                  {vehicle.imageUrls && vehicle.imageUrls.length > 0 && (
                    <CardMedia
                      component="img"
                      height="200"
                      image={vehicle.imageUrls[0]}
                      alt={`${vehicle.make} ${vehicle.model}`}
                    />
                  )}
                  <CardContent sx={{ flexGrow: 1 }}>
                    <Typography variant="h6" component="h3" gutterBottom>
                      {vehicle.make} {vehicle.model} ({vehicle.year})
                    </Typography>
                    <Typography variant="body2" color="text.secondary" paragraph>
                      {vehicle.company.companyName}
                    </Typography>
                    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 2 }}>
                      <Chip
                        label={vehicle.type.replace('_', ' ')}
                        size="small"
                        color="primary"
                        variant="outlined"
                      />
                      <Chip
                        label={`${vehicle.seatingCapacity} seats`}
                        size="small"
                        variant="outlined"
                      />
                      <Chip
                        label={vehicle.fuelType}
                        size="small"
                        variant="outlined"
                      />
                    </Box>
                    <Typography variant="h6" color="primary">
                      ${vehicle.dailyRate}/day
                    </Typography>
                  </CardContent>
                  <CardActions>
                    <Button
                      size="small"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleVehicleClick(vehicle.id);
                      }}
                    >
                      View Details
                    </Button>
                    <Button
                      size="small"
                      variant="contained"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleReserveClick(vehicle.id);
                      }}
                    >
                      Reserve
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}

        {!isLoading && !error && (vehicles || []).length === 0 && (
          <Box sx={{ textAlign: 'center', py: 4 }}>
            <Typography variant="h6" color="text.secondary">
              No vehicles found matching your criteria
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Try adjusting your search filters
            </Typography>
          </Box>
        )}
      </Container>
    </LocalizationProvider>
  );
};
