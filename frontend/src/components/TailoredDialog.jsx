import React, { useRef } from 'react';
import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  Button, IconButton, Tooltip, Box, Typography
} from '@mui/material';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import { downloadTextAsPDF } from '../utils/pdf';

export default function TailoredDialog({ open, onClose, text }) {
  const ref = useRef(null);

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(text || '');
    } catch (e) { /* ignore */ }
  }

  async function handlePdf() {
    await downloadTextAsPDF({ text, filename: 'tailored-resume.pdf' });
  }

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
      <DialogTitle>
        Tailored Resume
        <Tooltip title="Copy text">
          <IconButton sx={{ ml: 1 }} onClick={handleCopy}><ContentCopyIcon/></IconButton>
        </Tooltip>
        <Tooltip title="Download PDF">
          <IconButton sx={{ ml: 1 }} onClick={handlePdf}><PictureAsPdfIcon/></IconButton>
        </Tooltip>
      </DialogTitle>
      <DialogContent dividers>
        <Box ref={ref}>
          <Typography component="pre" sx={{
            whiteSpace: 'pre-wrap',
            fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace'
          }}>
            {text || 'No content'}
          </Typography>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} variant="contained">Close</Button>
      </DialogActions>
    </Dialog>
  );
}
