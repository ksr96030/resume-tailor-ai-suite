import React, { useState, useEffect } from 'react';
import {
  Container, Box, Grid, Typography, Paper, Stack, Chip, Link
} from '@mui/material';
import HealthAndSafetyIcon from '@mui/icons-material/HealthAndSafety';

import ResumeUploadCard from './components/ResumeUploadCard.jsx';
import JobDescriptionCard from './components/JobDescriptionCard.jsx';
import ResultPanel from './components/ResultPanel.jsx';
import TailoredDialog from './components/TailoredDialog.jsx';
import Toast from './components/Toast.jsx';

import { uploadResume, createJob, getAtsScore, tailorResume, pingAI } from './api';

export default function App() {
  const [resumeId, setResumeId] = useState(null);
  const [jobId, setJobId] = useState(null);
  const [atsScore, setAtsScore] = useState(null);
  const [tailored, setTailored] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [toast, setToast] = useState({ open: false, severity: 'info', message: '' });
  const [aiHealth, setAiHealth] = useState('UNKNOWN');

  useEffect(() => {
    (async () => {
      const status = await pingAI();
      setAiHealth(String(status).toUpperCase().includes('OK') ? 'OK' : String(status));
    })();
  }, []);

  function notify(message, severity = 'info') {
    setToast({ open: true, severity, message });
  }

  async function handleResumeUpload(file) {
    try {
      const res = await uploadResume(file);
      const id = res?.id ?? res?.resumeId ?? res?.resumeID;
      if (!id) throw new Error('Upload response missing id');
      setResumeId(id);
      setAtsScore(null);
      setTailored('');
      notify(`Uploaded resume (#${id})`, 'success');
    } catch (e) {
      notify(`Upload failed: ${e.message}`, 'error');
    }
  }

  async function handleSaveJob(desc) {
    try {
      const res = await createJob(desc);
      const id = res?.id ?? res?.jobId ?? res?.jobID;
      if (!id) throw new Error('Create job response missing id');
      setJobId(id);
      setAtsScore(null);
      setTailored('');
      notify(`Saved job description (#${id})`, 'success');
    } catch (e) {
      notify(`Save JD failed: ${e.message}`, 'error');
    }
  }

  async function handleGetAts() {
    try {
      const score = await getAtsScore(resumeId, jobId);
      setAtsScore(score);
      notify(`ATS Score: ${score}`, score >= 70 ? 'success' : 'warning');
    } catch (e) {
      notify(`ATS failed: ${e.message}`, 'error');
    }
  }

  async function handleTailor() {
    try {
      const res = await tailorResume(resumeId, jobId);
      setAtsScore(res.atsScore ?? null);
      setTailored(res.tailoredText || '');
      setDialogOpen(true);
      if (String(res.tailoredText).startsWith('HF API error')) {
        notify('HF API returned an error (showing message). Check your token/URL.', 'warning');
      } else {
        notify('Tailored resume generated', 'success');
      }
    } catch (e) {
      notify(`Tailor failed: ${e.message}`, 'error');
    }
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Paper sx={{ p: 3, mb: 3, borderRadius: 3 }} elevation={0} variant="outlined">
        <Stack direction="row" spacing={2} alignItems="center" justifyContent="space-between">
          <Box>
            <Typography variant="h4">AI Resume Tailoring Suite</Typography>
            <Typography variant="body2" color="text.secondary">
              Hybrid mode with Hugging Face fallback. Backend: Spring Boot. Frontend: React + MUI.
            </Typography>
          </Box>
          <Chip
            icon={<HealthAndSafetyIcon />}
            color={aiHealth === 'OK' ? 'success' : 'default'}
            label={`AI: ${aiHealth}`}
          />
        </Stack>
      </Paper>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <ResumeUploadCard onUploaded={handleResumeUpload} />
        </Grid>
        <Grid item xs={12} md={6}>
          <JobDescriptionCard onSaved={handleSaveJob} />
        </Grid>
        <Grid item xs={12}>
          <ResultPanel
            resumeId={resumeId}
            jobId={jobId}
            atsScore={atsScore}
            onGetAts={handleGetAts}
            onTailor={handleTailor}
          />
        </Grid>
      </Grid>

      <Box sx={{ textAlign: 'center', mt: 4, color: 'text.secondary' }}>
        <Typography variant="caption">
          Need help? Check your <code>VITE_API_BASE</code> in <code>.env</code> and CORS settings on the backend.
        </Typography>
        <Typography variant="caption" display="block">
          Built with ❤️ – <Link href="https://mui.com/" target="_blank" rel="noreferrer">Material UI</Link>
        </Typography>
      </Box>

      <TailoredDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        text={tailored}
      />

      <Toast
        open={toast.open}
        onClose={() => setToast((t) => ({ ...t, open: false }))}
        severity={toast.severity}
        message={toast.message}
      />
    </Container>
  );
}
