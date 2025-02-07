import { Outlet } from "react-router-dom";
import { Toaster } from "sonner";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
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
