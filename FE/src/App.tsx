import { Outlet } from "react-router-dom"
import Header from "./components/Header"
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'

function App() {
  const isLoggedIn = true

  return (
    <>
      <Header />
      <Outlet context={ {isLoggedIn} } />
      <ReactQueryDevtools initialIsOpen={false} />
    </>
  )
}

export default App
