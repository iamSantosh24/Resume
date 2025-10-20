const express = require('express');
const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');
const bodyParser = require('body-parser');
const https = require('https');

const app = express();
app.use(bodyParser.json());

const PORT = process.env.PORT || 4000;
const SERVICE_ACCOUNT_PATH = process.env.SERVICE_ACCOUNT_PATH || process.env.GOOGLE_APPLICATION_CREDENTIALS;
const DATABASE_URL = process.env.FIREBASE_DATABASE_URL || 'https://resume-b707f-default-rtdb.firebaseio.com';

let useAdmin = false;

function tryInitAdmin() {
  // Try environment first, but if missing, allow a local fallback file at server-admin/serviceAccountKey.json
  const envSa = SERVICE_ACCOUNT_PATH;
  let saPathToTry = envSa;

  if (!saPathToTry) {
    const fallback = path.join(__dirname, 'serviceAccountKey.json');
    if (fs.existsSync(fallback)) {
      saPathToTry = fallback;
      console.log('[admin-server] Found local serviceAccountKey.json at', fallback, '- using it to initialize Admin SDK.');
    } else {
      console.log('[admin-server] No SERVICE_ACCOUNT_PATH or GOOGLE_APPLICATION_CREDENTIALS set and no local serviceAccountKey.json found; admin SDK will not be used.');
      return;
    }
  }

  try {
    const saPathResolved = path.resolve(saPathToTry);
    if (!fs.existsSync(saPathResolved)) {
      console.warn('[admin-server] Service account file not found at', saPathResolved);
      return;
    }

    const serviceAccount = require(saPathResolved);
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
      databaseURL: DATABASE_URL
    });
    useAdmin = true;
    console.log('[admin-server] Firebase Admin initialized, DB URL =', DATABASE_URL);
  } catch (err) {
    console.error('[admin-server] Failed to initialize Firebase Admin SDK:', err.message || err);
  }
}

tryInitAdmin();

const localDataPath = path.join(__dirname, 'resume.json');
function readLocalResume() {
  try {
    if (fs.existsSync(localDataPath)) {
      const raw = fs.readFileSync(localDataPath, 'utf8');
      return JSON.parse(raw);
    }
  } catch (e) {
    console.warn('[admin-server] Error reading local resume.json:', e.message);
  }
  // default sample
  return {
    name: "Santosh Example",
    title: "Android Developer",
    summary: "A concise summary about me.",
    skills: [
      { name: "Kotlin", level: "Expert" },
      { name: "Jetpack Compose", level: "Advanced" }
    ]
  };
}

function writeLocalResume(obj) {
  try {
    fs.writeFileSync(localDataPath, JSON.stringify(obj, null, 2), 'utf8');
    return true;
  } catch (e) {
    console.error('[admin-server] Error writing local resume.json:', e.message);
    return false;
  }
}

function normalizeSkillsNode(data) {
  try {
    if (!data || typeof data !== 'object') return data;
    const skills = data.skills;
    if (!skills) return data;
    // If skills is already an array, nothing to do
    if (Array.isArray(skills)) return data;
    // If skills is an object (e.g. { firebase_services: [...], frameworks_libraries: [...] }),
    // flatten into an array of { name: string, level?: string } entries so the Android client can parse it.
    if (typeof skills === 'object') {
      const out = [];
      for (const [category, items] of Object.entries(skills)) {
        if (Array.isArray(items)) {
          for (const item of items) {
            // item might be string or object; map to a Skill-like object
            if (typeof item === 'string') {
              out.push({ name: item, level: category });
            } else if (typeof item === 'object' && item !== null) {
              // if item already looks like { name, level } or similar, preserve name and level if present
              const name = item.name || item.title || null;
              const level = item.level || category;
              if (name) out.push({ name, level });
            }
          }
        } else if (typeof items === 'object' && items !== null) {
          // items is an object mapping ids to skill entries
          for (const entry of Object.values(items)) {
            if (typeof entry === 'string') {
              out.push({ name: entry, level: category });
            } else if (typeof entry === 'object' && entry !== null) {
              const name = entry.name || entry.title || null;
              const level = entry.level || category;
              if (name) out.push({ name, level });
            }
          }
        }
      }
      data.skills = out;
    }
  } catch (e) {
    console.warn('[admin-server] normalizeSkillsNode error:', e && e.message ? e.message : e);
  }
  return data;
}

// New: ensure a field is returned as an array of objects
function ensureArray(node) {
  if (!node) return [];
  if (Array.isArray(node)) return node;
  if (typeof node === 'object') return Object.values(node);
  // single primitive -> wrap
  return [node];
}

function normalizeResume(raw) {
  try {
    if (!raw || typeof raw !== 'object') return raw;
    const data = Object.assign({}, raw);

    // Normalize skills (use existing logic)
    const normalizedSkills = normalizeSkillsNode(JSON.parse(JSON.stringify({ skills: data.skills }))).skills;
    if (normalizedSkills) data.skills = normalizedSkills;

    // Normalize education, experience, projects to arrays of objects
    data.education = ensureArray(data.education).map(e => (typeof e === 'object' && e !== null) ? e : { university: String(e) });
    data.experience = ensureArray(data.experience).map(e => (typeof e === 'object' && e !== null) ? e : { company: String(e) });
    data.projects = ensureArray(data.projects).map(p => (typeof p === 'object' && p !== null) ? p : { name: String(p) });

    // Ensure personalInfo exists and pull top-level name/summary into it if missing
    if (!data.personalInfo || typeof data.personalInfo !== 'object') {
      data.personalInfo = {};
    }
    if (!data.personalInfo.name && data.name) data.personalInfo.name = data.name;
    if (!data.personalInfo.summary && data.summary) data.personalInfo.summary = data.summary;

    return data;
  } catch (e) {
    console.warn('[admin-server] normalizeResume error:', e && e.message ? e.message : e);
    return raw;
  }
}

app.get('/health', (req, res) => res.json({ ok: true, adminInitialized: useAdmin }));

app.get('/resume', async (req, res) => {
  if (useAdmin) {
    try {
      const db = admin.database();
      // Read from the new RTDB path 'users/myResumeProfile'
      const snapshot = await db.ref('users/myResumeProfile').once('value');
      const data = snapshot.val();
      if (data) {
        const normalized = normalizeResume(JSON.parse(JSON.stringify(data)));
        return res.json(normalized);
      }
      // fallback to local
      return res.json(readLocalResume());
    } catch (e) {
      console.error('[admin-server] Error reading from RTDB:', e.message || e);
      return res.status(500).json({ error: 'Failed to read from Firebase Realtime Database', detail: e.message || e });
    }
  }

  // not using admin: try public REST endpoint on the RTDB first, then fallback to local
  async function fetchPublicResume() {
    return new Promise((resolve) => {
      try {
        // Ensure DATABASE_URL ends without a trailing slash before appending
        const base = DATABASE_URL.endsWith('/') ? DATABASE_URL.slice(0, -1) : DATABASE_URL;
        // Use the new path for public REST GET
        const url = base + '/users/myResumeProfile.json';
        const req = https.get(url, { timeout: 5000 }, (resp) => {
          let data = '';
          resp.on('data', (chunk) => (data += chunk));
          resp.on('end', () => {
            try {
              const parsed = JSON.parse(data);
              // Normalize the whole resume shape if needed
              resolve(normalizeResume(parsed));
            } catch (e) {
              console.warn('[admin-server] Failed to parse public RTDB response:', e.message || e);
              resolve(null);
            }
          });
        });
        req.on('error', (err) => {
          console.warn('[admin-server] Error fetching public RTDB:', err.message || err);
          resolve(null);
        });
        req.on('timeout', () => {
          req.destroy();
          console.warn('[admin-server] Timeout fetching public RTDB');
          resolve(null);
        });
      } catch (e) {
        console.warn('[admin-server] Exception while fetching public RTDB:', e.message || e);
        resolve(null);
      }
    });
  }

  try {
    const publicData = await fetchPublicResume();
    if (publicData && Object.keys(publicData).length > 0) {
      return res.json(publicData);
    }
  } catch (e) {
    console.warn('[admin-server] Unexpected error when trying public RTDB:', e.message || e);
  }

  // final fallback: local file
  return res.json(readLocalResume());
});

app.post('/resume', async (req, res) => {
  const payload = req.body;
  if (!payload) return res.status(400).json({ error: 'Missing JSON body' });

  if (useAdmin) {
    try {
      const db = admin.database();
      // Write to the new RTDB path 'users/myResumeProfile'
      await db.ref('users/myResumeProfile').set(payload);
      return res.json({ ok: true });
    } catch (e) {
      console.error('[admin-server] Error writing to RTDB:', e.message || e);
      return res.status(500).json({ error: 'Failed to write to Firebase Realtime Database', detail: e.message || e });
    }
  }

  const ok = writeLocalResume(payload);
  if (!ok) return res.status(500).json({ error: 'Failed to write local resume.json' });
  return res.json({ ok: true });
});

app.listen(PORT, () => {
  console.log(`[admin-server] Listening on http://localhost:${PORT}`);
  console.log('[admin-server] useAdmin =', useAdmin, 'SERVICE_ACCOUNT_PATH =', SERVICE_ACCOUNT_PATH || '(none)');
});
