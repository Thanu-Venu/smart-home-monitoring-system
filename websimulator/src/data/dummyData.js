export const home = {
  id: "home001",
  name: "My Home",
  ownerId: "user001",
};

export const rooms = [
  {
    id: "livingRoom",
    name: "Living Room",
  },
  {
    id: "bedroom",
    name: "Bedroom",
  },
  {
    id: "kitchen",
    name: "Kitchen",
  },
  {
    id: "bathroom",
    name: "Bathroom",
  },
];

export const devices = [
  {
    id: "device001",
    name: "Living Room Light",
    type: "light",
    roomId: "livingRoom",
    status: true,
  },
  {
    id: "device002",
    name: "Living Room Fan",
    type: "fan",
    roomId: "livingRoom",
    status: false,
  },
  {
    id: "device003",
    name: "Bedroom Light",
    type: "light",
    roomId: "bedroom",
    status: true,
  },
  {
    id: "device004",
    name: "Clothing Iron",
    type: "iron",
    roomId: "bedroom",
    status: false,
    maxOnDuration: 300,
  },
  {
    id: "device005",
    name: "Kitchen Outlet",
    type: "outlet",
    roomId: "kitchen",
    status: false,
  },
  {
    id: "device006",
    name: "Living Room Camera",
    type: "camera",
    roomId: "livingRoom",
    status: true,
  },
];