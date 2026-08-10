import { ref, onValue } from "firebase/database";
import { db } from "../firebase/firebaseConfig";

export const listenToHome = (callback) => {
  const homeRef = ref(db, "homes/home001");

  return onValue(homeRef, (snapshot) => {
    const data = snapshot.val();
    callback(data);
  });
};