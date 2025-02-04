import { Outlet } from "react-router-dom";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import Header from "./components/Header";

function App() {
  const isLoggedIn = true;

  return (
    <>
      <Outlet context={{ isLoggedIn }} />
      <ReactQueryDevtools initialIsOpen={false} />
    </>
  );
}

export default App;
