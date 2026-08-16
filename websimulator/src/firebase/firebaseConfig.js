import { initializeApp } from "firebase/app";
import { getDatabase } from "firebase/database";
import { getAuth, signInAnonymously, onAuthStateChanged } from "firebase/auth";

const firebaseConfig = {
  apiKey: "AIzaSyCtZl1SiWzx8qT__tTC-Md0aaJJ3kl-dsE",
  authDomain: "smart-home-monitoring-sy-f5e88.firebaseapp.com",
  databaseURL: "https://smart-home-monitoring-sy-f5e88-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "smart-home-monitoring-sy-f5e88",
  storageBucket: "smart-home-monitoring-sy-f5e88.firebasestorage.app",
  messagingSenderId: "353055651866",
  appId: "1:353055651866:web:62700ac4dca93ede02b9a3"
};

const app = initializeApp(firebaseConfig);

export const db = getDatabase(app);
export const auth = getAuth(app);

/*
 * The Realtime Database rules require auth != null on every
 * read/write to "homes" (added so the Android app's per-user data
 * is protected). This simulator has no login screen of its own — it
 * is a shared "hardware dashboard", not a per-user client — so it
 * signs in anonymously instead. That's enough to satisfy the rules
 * (Firebase still issues a real, unique auth UID) without asking
 * whoever opens the simulator to create an account.
 *
 * ensureSignedIn() resolves once a session exists, so pages can wait
 * for it before starting their Firebase listeners instead of firing
 * a read that the rules would reject.
 */
export const ensureSignedIn = () => {

  return new Promise((resolve, reject) => {

    const unsubscribe = onAuthStateChanged(auth, (user) => {

      unsubscribe();

      if (user) {
        resolve(user);
        return;
      }

      signInAnonymously(auth)
        .then((credential) => resolve(credential.user))
        .catch(reject);
    });
  });
};
