import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../../api/auth";

function LoginPage({ setUser }) {
    const navigate = useNavigate();

    const [credentials, setCredentials] = useState({
        username: "",
        password: "",
    });
    const [errors, setErrors] = useState([]);

    function handleChange(event) {
        setCredentials({ ...credentials, [event.target.name]: event.target.value });
    }

    async function handleSubmit(event) {
        event.preventDefault();
        const result = await login(credentials.username, credentials.password);

        if (result.ok) {
            setUser(result.payload);
            navigate(result.payload.role === "AGENT" ? "/queue" : "/");
        } else {
            setErrors(result.payload);
        }
    }

    return (
        <>
            <h4>Log into your account</h4>
            <div className="row">
                <div className="col-3" />

                <form className="col-6" onSubmit={handleSubmit}>
                    {errors.length > 0 && (
                        <ul className="alert alert-danger">
                            {errors.map((error) => (
                                <li key={error}>{error}</li>
                            ))}
                        </ul>
                    )}

                    <div className="mb-3">
                        <label className="form-label" htmlFor="username-input">
                            Username
                        </label>
                        <input
                            className="form-control"
                            type="text"
                            id="username-input"
                            name="username"
                            onChange={handleChange}
                            value={credentials.username}
                        />
                    </div>

                    <div className="mb-3">
                        <label className="form-label" htmlFor="password-input">
                            Password
                        </label>
                        <input
                            className="form-control"
                            type="password"
                            id="password-input"
                            name="password"
                            onChange={handleChange}
                            value={credentials.password}
                        />
                    </div>

                    <button className="btn btn-primary" type="submit">
                        Log in!
                    </button>
                </form>

                <div className="col-3" />
            </div>
        </>
    );
}

export default LoginPage;
