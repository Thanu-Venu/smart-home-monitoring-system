import { initializeApp } from "firebase/app";
import { getDatabase } from "firebase/database";

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