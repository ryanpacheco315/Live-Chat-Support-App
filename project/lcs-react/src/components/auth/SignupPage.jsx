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
            navigate("/login");
        } else {
            setErrors(result.payload);
        }
    }

    return (
        <div className="row">
            <div className="col-md-3" />

            <div className="col-md-6">
                <h4 className="mb-3 text-center">
                    <i className="bi bi-person-plus me-2" aria-hidden="true" />
                    Sign up for an account
                </h4>
                <div className="card">
                    <div className="card-body">
                        <form onSubmit={handleSubmit}>
                            {errors.length > 0 && (
                                <ul className="alert alert-danger">
                                    {errors.map((error) => (
                                        <li key={error}>{error}</li>
                                    ))}
                                </ul>
                            )}

                            <div className="mb-3">
                                <label className="form-label" htmlFor="fullName-input">
                                    Full name
                                </label>
                                <input
                                    className="form-control"
                                    type="text"
                                    id="fullName-input"
                                    name="fullName"
                                    onChange={handleChange}
                                    value={user.fullName}
                                />
                            </div>

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
                                    value={user.username}
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
                                    value={user.password}
                                />
                            </div>

                            <button className="btn btn-primary w-100" type="submit">
                                Sign up
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <div className="col-md-3" />
        </div>
    );
}

export default SignupPage;
