import { Outlet } from "react-router-dom"
import Header from "./components/Header"

function App() {
  const isLoggedIn = true

  return (
    <>
      <Header />
      <Outlet context={ {isLoggedIn} } />
    </>
  )
}

export default App
