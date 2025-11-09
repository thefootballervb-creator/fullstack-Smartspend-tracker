import { Link } from "react-router-dom";
import success from '../../../assets/images/success.gif'

function RegistrationSuccess() {

    return (

        <div className='container'>
            <div className="auth-form">
                <img src={success} alt="Registration successful animation" size='20px'/>
                <h4 style={{textAlign:"center", color: "green"}}>Congratulations, You account has been successfully created!</h4>
                <br/>
                <Link to='/auth/login'><button className="button button-fill" style={{padding: '7px 25px'}}>Login now</button></Link>
            </div>
        </div>
    )
}
export default RegistrationSuccess;