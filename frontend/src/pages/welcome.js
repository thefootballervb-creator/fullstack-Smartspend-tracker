import { Link } from 'react-router-dom';
import '../assets/styles/welcome.css';
import Logo from '../components/utils/Logo';

function Welcome() {
    return (
        <section className="hero-section">
            <Logo/>
            <h2>Welcome to My Pockit!</h2>
            <h3>Pockit — power up your pocket!
            Track your money, plan your spending, and stay in control — anytime, anywhere.</h3>
            <div>
                <Link to='/auth/login'><p><button>Log in</button></p></Link>
                <Link to='/auth/register'><button>Create Account</button></Link>
            </div>
        </section>
    )
}

export default Welcome;