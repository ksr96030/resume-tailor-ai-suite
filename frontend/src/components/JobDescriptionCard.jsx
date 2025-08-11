import React, { useState } from 'react';
import { Card, CardHeader, CardContent, TextField, Stack, Button, LinearProgress } from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';

export default function JobDescriptionCard({ onSaved }) {
  const [desc, setDesc] = useState('');
  const [busy, setBusy] = useState(false);

  async function handleSave() {
    if (!desc.trim()) return;
    setBusy(true);
    try {
      await onSaved(desc);
    } finally {
      setBusy(false);
    }
  }

  return (
    <Card variant="outlined" sx={{ height: '100%' }}>
      <CardHeader title="2) Paste Job Description" subheader="Paste or type the JD text below" />
      <CardContent>
        <Stack spacing={2}>
          <TextField
            label="Job Description"
            multiline
            minRows={8}
            fullWidth
            value={desc}
            onChange={(e) => setDesc(e.target.value)}
            placeholder="Paste the full job description here..."
          />
          <Button variant="contained" startIcon={<SaveIcon />} onClick={handleSave} disabled={busy || !desc.trim()}>
            Save Job Description
          </Button>
          {busy && <LinearProgress />}
        </Stack>
      </CardContent>
    </Card>
  );
}
