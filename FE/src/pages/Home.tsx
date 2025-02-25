import { useOutletContext } from "react-router-dom";
import LoggedInHome from "../components/LoggedInHome";
import LoggedOutHome from "../components/LoggedOutHome";

interface OuletContextType {
    isLoggedIn: boolean,
}

function Home(){
    const {isLoggedIn} = useOutletContext<OuletContextType>();

    return (
        <>
            <h1>Home page</h1>
            {isLoggedIn ? <LoggedInHome /> : <LoggedOutHome />}
        </>
    )
}

export default Home;