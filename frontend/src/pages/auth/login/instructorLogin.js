import {useEffect, useState} from 'react';
import '../../../assets/styles/instructorLogin.css';
import {useForm} from 'react-hook-form';
import { useNavigate} from 'react-router-dom';
import AuthService from '../../../services/auth.service';

function InstructorLogin() {

    const navigate = useNavigate();

    useEffect(() => {
        if (AuthService.getCurrentUser() && AuthService.getCurrentUser().roles.includes("ROLE_USER")) {
            navigate("/user/dashboard");
        }else if (AuthService.getCurrentUser() && AuthService.getCurrentUser().roles.includes("ROLE_ADMIN")) {
            navigate("/admin/transactions");
        }
    }, [navigate])


    const {register, handleSubmit, formState} = useForm();

    const [response_error, setResponseError] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const onSubmit = async (data) => {
        setIsLoading(true);
        setResponseError("");
        
        try {
            await AuthService.login_req(data.email, data.password);
            setResponseError("");
            
            setTimeout(() => {
                if (AuthService.getCurrentUser()) {
                    const roles = AuthService.getCurrentUser().roles;
                    if (roles.includes("ROLE_USER")) {
                        navigate("/user/dashboard");
                    } else if (roles.includes("ROLE_ADMIN")) {
                        navigate("/admin/transactions");
                    } else {
                        navigate("/");
                    }
                }
            }, 500);
            
            localStorage.setItem("message", JSON.stringify({ status: "SUCCESS", text: "Login successful!" }))
        } catch (error) {
            console.error("Login error:", error);
            
            // Check for network errors first
            if (error.code === 'ERR_NETWORK' || 
                error.code === 'ERR_CONNECTION_REFUSED' ||
                error.message?.includes('Failed to fetch') ||
                error.message?.includes('Network Error') ||
                (!error.response && error.request)) {
                setResponseError("Network error. Please try again.");
            } else if (error.response) {
                // Server responded with error status
                const resMessage = error.response.data?.message || error.response.statusText || 'Unknown error';
                
                if (resMessage === "Bad credentials" || error.response.status === 401) {
                    setResponseError("Invalid email or password!");
                } else {
                    setResponseError(`Error: ${resMessage}`);
                }
            } else {
                // Other errors
                setResponseError("Something went wrong: Try again later!");
            }
        } finally {
            setIsLoading(false);
        }
    }

    return(
        <div className='instructor-login-container'>
            <form className="instructor-login-form" onSubmit={handleSubmit(onSubmit)}>
                <h2 className="instructor-login-title">Instructor Login</h2>
                
                <div className='instructor-input-box'>
                    <label>Email</label>
                    <input 
                        type='text'
                        defaultValue="instructor@gmail.com"
                        {...register('email', {
                            required: "Email is required!",
                            pattern: {value:/^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$/g, message:"Invalid email address!"}
                        })}
                    />
                    {formState.errors.email && <small>{formState.errors.email.message}</small>}
                </div>
                
                <div className='instructor-input-box'>
                    <label>Password</label>
                    <input 
                        type='password'
                        placeholder="Enter your password"
                        {...register('password', {
                            required: 'Password is required!'
                        })}
                    />
                    {formState.errors.password && <small>{formState.errors.password.message}</small>}
                </div>
                
                {
                    (response_error !== "") && (
                        <p className="instructor-error-message">{response_error}</p>
                    )
                }
                
                <div className='instructor-submit-box'>
                    <button 
                        type='submit' 
                        className={`instructor-signin-button ${isLoading ? 'loading' : ''}`}
                        disabled={isLoading}
                    >
                        {isLoading ? "Signing in..." : 'Sign In'}
                    </button>
                </div>
            </form>
        </div>
    )
}

export default InstructorLogin;

