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
        <>
            <h4>Create an Agent</h4>
            <div className="row">
                <div className="col-3" />

                <form className="col-6" onSubmit={handleSubmit}>
                    {successMessage && <p className="text-success">{successMessage}</p>}
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
                        <button type="submit">Create Agent</button>
                    </div>
                </form>

                <div className="col-3" />
            </div>
        </>
    );
}

export default CreateAgentPage;
