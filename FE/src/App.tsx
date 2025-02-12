// import { Outlet } from "react-router-dom";
// import { Toaster } from "sonner";
// import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
// function App() {
//   return (
//     <>
//       <Toaster />
//       <Outlet />
//       <ReactQueryDevtools initialIsOpen={false} />
//     </>
//   );
// }

// export default App;

import { Outlet } from "react-router-dom";
import { Toaster } from "sonner";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { SocketProvider } from "./components/SocketContext";

function App() {
  return (
    <>
      <Toaster />
      <Outlet />
      <ReactQueryDevtools initialIsOpen={false} />
    </>
  );
}

export default App;
// import { Outlet } from "react-router-dom";
// import { Toaster } from "sonner";
// import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
// import { SocketProvider } from "./components/SocketContext";

// function App() {
//   return (
//     <SocketProvider url="https://i12b212.p.ssafy.io">
//       <Toaster />
//       <Outlet />
//       <ReactQueryDevtools initialIsOpen={false} />
//     </SocketProvider>
//   );
// }

// export default App;