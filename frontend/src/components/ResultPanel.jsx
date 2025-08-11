import React from 'react';
import { Card, CardHeader, CardContent, Stack, Button, Chip, Typography, Divider } from '@mui/material';
import CalculateIcon from '@mui/icons-material/Calculate';
import AutoFixHighIcon from '@mui/icons-material/AutoFixHigh';

export default function ResultPanel({
  resumeId,
  jobId,
  atsScore,
  onGetAts,
  onTailor
}) {
  const disabled = !resumeId || !jobId;

  return (
    <Card variant="outlined" sx={{ height: '100%' }}>
      <CardHeader
        title="3) Analyze & Tailor"
        subheader="Get ATS score and generate a tailored resume"
        action={
          <Stack direction="row" spacing={1} alignItems="center">
            <Chip label={`Resume ID: ${resumeId ?? '-'}`} />
            <Chip label={`Job ID: ${jobId ?? '-'}`} />
            {typeof atsScore === 'number' && (
              <Chip color={atsScore >= 70 ? 'success' : atsScore >= 50 ? 'warning' : 'default'}
                    label={`ATS: ${atsScore}`} />
            )}
          </Stack>
        }
      />
      <CardContent>
        <Stack spacing={2} direction="row" sx={{ flexWrap: 'wrap', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<CalculateIcon />}
            onClick={onGetAts}
            disabled={disabled}
          >
            Get ATS Score
          </Button>
          <Button
            variant="contained"
            startIcon={<AutoFixHighIcon />}
            onClick={onTailor}
            disabled={disabled}
          >
            Tailor Resume
          </Button>
        </Stack>
        <Divider sx={{ my: 2 }} />
        <Typography variant="body2" color="text.secondary">
          Tip: Aim for ATS ≥ 70 by matching key skills and phrasing from the JD.
        </Typography>
      </CardContent>
    </Card>
  );
}
