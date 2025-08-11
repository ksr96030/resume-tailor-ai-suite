import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080',
  timeout: 60000,
});

// ---- Endpoints ----
// 1) Upload resume (multipart)
//    returns { id, fileName?, content? }
export async function uploadResume(file) {
  const form = new FormData();
  form.append('file', file);
  const { data } = await api.post('/api/resume/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  return data;
}

// 2) Create job description
//    body: { description: string }
//    returns { id, description? }
export async function createJob(description, opts = {}) {
  const params = {
    title: opts.title || 'Job Position',
    company: opts.company || 'Company',
    location: opts.location || '',
    employmentType: opts.employmentType || 'Full-time',
    experienceLevel: opts.experienceLevel || 'Mid-level',
  };

  const { data } = await api.post(
    `/api/job/upload`,
    description,
    {
      headers: { 'Content-Type': 'text/plain' },
      params
    }
  );
  return data;
}

// 3) ATS score
//    GET /api/resume/ats?resumeId=&jobId=
//    BE might return {atsScore} or just number
export async function getAtsScore(resumeId, jobId) {
  const { data } = await api.get('/api/resume/ats-score', {
    params: { resumeId, jobId }
  });

  // Prefer detailedScore (AI-enhanced), then basicScore (keyword), then any fallbacks
  if (typeof data?.detailedScore === 'number') return data.detailedScore;
  if (typeof data?.basicScore === 'number') return data.basicScore;
  if (typeof data?.atsScore === 'number') return data.atsScore; // legacy
  if (typeof data?.score === 'number') return data.score;       // legacy
  return 0;
}

// 4) Tailor resume
//    POST /api/resume/tailor { resumeId, jobId }
//    returns { resumeId, jobId, atsScore, tailoredText }
export async function tailorResume(resumeId, jobId) {
  const { data } = await api.post('/api/resume/tailor', { resumeId, jobId });
  // Normalize fields just in case
  return {
    resumeId: data.resumeId ?? resumeId,
    jobId: data.jobId ?? jobId,
    atsScore: typeof data.atsScore === 'number' ? data.atsScore : (data.score ?? 0),
    tailoredText: data.tailoredText ?? String(data ?? '')
  };
}

// Optional ping
export async function pingAI() {
  try {
    const { data } = await api.get('/api/ai/ping');
    return data;
  } catch {
    return 'DOWN';
  }
}
