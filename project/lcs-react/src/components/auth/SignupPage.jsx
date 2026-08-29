import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { signup } from "../../api/auth";

function SignupPage() {
    const navigate = useNavigate();

    const [user, setUser] = useState({
        fullName: "",
        username: "",
        password: "",
    });
    const [errors, setErrors] = useState([]);

    function handleChange(event) {
        setUser({ ...user, [event.target.name]: event.target.value });
    }

    async function handleSubmit(event) {
        event.preventDefault();
        const result = await signup(user);

        if (result.ok) {
            // Signup doesn't start a session (only /login does), so send them to log in.
            navigate("/login");
        } else {
            setErrors(result.payload);
        }
    }

    return (
        <>
            <h4>Sign up for an account</h4>
            <div className="row">
                <div className="col-3" />

                <form className="col-6" onSubmit={handleSubmit}>
                    {errors.length > 0 && (
                        <ul>
                            {errors.map((error) => (
                                <li key={error}>{error}</li>
                            ))}
                        </ul>
                    )}

                    <div className="form-control">
                        <label htmlFor="fullName-input">Full name: </label>
                        <input
                            type="text"
                            id="fullName-input"
                            name="fullName"
                            onChange={handleChange}
                            value={user.fullName}
                        />
                    </div>

                    <div className="form-control">
                        <label htmlFor="username-input">Username: </label>
                        <input
                            type="text"
                            id="username-input"
                            name="username"
                            onChange={handleChange}
                            value={user.username}
                        />
                    </div>

                    <div className="form-control">
                        <label htmlFor="password-input">Password: </label>
                        <input
                            type="password"
                            id="password-input"
                            name="password"
                            onChange={handleChange}
                            value={user.password}
                        />
                    </div>

                    <div className="form-control">
                        <button type="submit">Sign up!</button>
                    </div>
                </form>

                <div className="col-3" />
            </div>
        </>
    );
}

export default SignupPage;
