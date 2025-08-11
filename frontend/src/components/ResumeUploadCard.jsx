import React, { useRef, useState } from 'react';
import { Card, CardHeader, CardContent, Stack, Button, Typography, LinearProgress } from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';

export default function ResumeUploadCard({ onUploaded }) {
  const inputRef = useRef(null);
  const [fileName, setFileName] = useState('');
  const [busy, setBusy] = useState(false);

  async function handleFile(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    setFileName(file.name);
    setBusy(true);
    try {
      await onUploaded(file);
    } finally {
      setBusy(false);
    }
  }

  return (
    <Card variant="outlined" sx={{ height: '100%' }}>
      <CardHeader title="1) Upload Resume" subheader="PDF, DOC, DOCX, or TXT" />
      <CardContent>
        <Stack spacing={2}>
          <input
            ref={inputRef}
            type="file"
            accept=".pdf,.doc,.docx,.txt"
            hidden
            onChange={handleFile}
          />
          <Button
            variant="contained"
            startIcon={<CloudUploadIcon />}
            onClick={() => inputRef.current?.click()}
            disabled={busy}
          >
            Choose File
          </Button>
          {fileName && <Typography variant="body2">Selected: {fileName}</Typography>}
          {busy && <LinearProgress />}
        </Stack>
      </CardContent>
    </Card>
  );
}
