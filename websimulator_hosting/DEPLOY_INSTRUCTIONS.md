# Hosting the Web Simulator on Firebase Hosting

This uses **Firebase Hosting** — free, and it's the same Firebase project you're already using for the database and auth, so there's nothing new to set up on the backend side.

Your project ID (already filled into the config files below): `smart-home-monitoring-sy-f5e88`

## 1. Add these two files to your `websimulator/` folder

Copy `firebase.json` and `.firebaserc` (both included in this zip) into your local `websimulator/` folder — the same folder that contains `package.json`. They just tell Firebase which folder to publish (`dist/`) and which project to publish it to.

## 2. Install the Firebase CLI (one-time)

Open a terminal on your computer and run:

```bash
npm install -g firebase-tools
```

## 3. Log in to Firebase (one-time)

```bash
firebase login
```

This opens a browser window — sign in with the same Google account that owns the `smart-home-monitoring-sy-f5e88` Firebase project. (I can't do this step for you — it has to be your own login.)

## 4. Build and deploy

From inside your `websimulator/` folder:

```bash
npm install
npm run build
firebase deploy --only hosting
```

The first `firebase deploy` may take a minute — it uploads the `dist/` folder Vite just built. When it finishes, it prints your live URL, which will be:

```
https://smart-home-monitoring-sy-f5e88.web.app
```

(also reachable at `https://smart-home-monitoring-sy-f5e88.firebaseapp.com`)

## Publishing future changes

Any time you change the web simulator's source code, republish with:

```bash
npm run build
firebase deploy --only hosting
```

## Notes

- The `apiKey` visible in `firebaseConfig.js` is safe to expose publicly — Firebase web API keys aren't secret credentials, they just identify which project a request belongs to. Actual access is enforced by your `database.rules.json` (which already requires `auth != null`), not by hiding the key.
- Since the site is a single-page React app, `firebase.json` includes a rewrite rule sending every route to `index.html` — without it, refreshing the browser on a route like `/floors` would 404.
- If you ever want a custom domain instead of the `.web.app` one, that's set up from Firebase Console → Hosting → Add custom domain — no code changes needed.
