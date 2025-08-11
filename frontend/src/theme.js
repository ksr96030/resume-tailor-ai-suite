import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  shape: { borderRadius: 14 },
  palette: {
    mode: 'light',
    primary: { main: '#1a73e8' },
    secondary: { main: '#00a389' }
  },
  typography: {
    fontFamily: ['Inter', 'system-ui', 'Segoe UI', 'Roboto', 'Arial'].join(','),
    h4: { fontWeight: 700 },
  }
});

export default theme;
