import { useState } from "react";
import { createAgent } from "../../api/admin";

const initialForm = { fullName: "", username: "", password: "" };

function CreateAgentPage() {
    const [user, setUser] = useState(initialForm);
    const [errors, setErrors] = useState([]);
    const [successMessage, setSuccessMessage] = useState(null);

    function handleChange(event) {
        setUser({ ...user, [event.target.name]: event.target.value });
    }

    async function handleSubmit(event) {
        event.preventDefault();
        const result = await createAgent(user);

        if (result.ok) {
            setSuccessMessage(`Agent "${result.payload.username}" created.`);
            setErrors([]);
            setUser(initialForm);
        } else {
            setSuccessMessage(null);
            setErrors(result.payload ?? ["Something went wrong. Please try again."]);
        }
    }

    return (
        <div className="row">
            <div className="col-md-3" />

            <div className="col-md-6">
                <h4 className="mb-3">Create an Agent</h4>
                <div className="card">
                    <div className="card-body">
                        <form onSubmit={handleSubmit}>
                            {successMessage && (
                                <div className="alert alert-success d-flex align-items-center">
                                    <i className="bi bi-check-circle me-2" aria-hidden="true" />
                                    {successMessage}
                                </div>
                            )}
                            {errors.length > 0 && (
                                <ul className="alert alert-danger mb-3">
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

                            <button className="btn btn-primary" type="submit">
                                <i className="bi bi-person-plus me-1" aria-hidden="true" />
                                Create Agent
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <div className="col-md-3" />
        </div>
    );
}

export default CreateAgentPage;
