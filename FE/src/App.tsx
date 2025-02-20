<<<<<<< HEAD
import { Outlet } from "react-router-dom"
import Header from "./components/Header"
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'

function App() {
  const isLoggedIn = true

  return (
    <>
      <Header />
      <Outlet context={ {isLoggedIn} } />
=======
import { Outlet } from "react-router-dom";
import { Toaster } from "sonner";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";

function App() {
  return (
    <>
      <Toaster richColors={true} theme="dark" />
      <Outlet />
      {/* <ReactQueryDevtools initialIsOpen={false} /> */}
    </>
  )
}

export default App
