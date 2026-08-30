import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { startChat } from "../../api/chats";

function StartChatPage() {
    const navigate = useNavigate();

    const [problem, setProblem] = useState({
        category: "HARDWARE",
        subcategory: "",
        description: "",
    });
    const [errors, setErrors] = useState([]);

    function handleChange(event) {
        setProblem({ ...problem, [event.target.name]: event.target.value });
    }

    async function handleSubmit(event) {
        event.preventDefault();
        const result = await startChat(problem);

        if (result.ok) {
            navigate("/waiting", { state: { chat: result.payload } });
        } else {
            setErrors(result.payload ?? ["Something went wrong. Please try again."]);
        }
    }

    return (
        <>
            <h4>Tell us what is wrong</h4>
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

                    <div className="form-control">
                        <label htmlFor="category-input">Category:</label>
                        <select
                            name="category"
                            id="category-input"
                            value={problem.category}
                            onChange={handleChange}
                        >
                            <option value="HARDWARE">Hardware</option>
                            <option value="SOFTWARE">Software</option>
                            <option value="OTHER">Other</option>
                        </select>
                    </div>

                    <div className="form-control">
                        <label htmlFor="subcategory-input">Subcategory (optional):</label>
                        <input
                            type="text"
                            id="subcategory-input"
                            name="subcategory"
                            onChange={handleChange}
                            value={problem.subcategory}
                        />
                    </div>

                    <div className="form-control">
                        <label htmlFor="description-input">Describe the problem:</label>
                        <textarea
                            id="description-input"
                            name="description"
                            onChange={handleChange}
                            value={problem.description}
                        />
                    </div>

                    <div className="form-control">
                        <button type="submit">Start Chat</button>
                    </div>
                </form>

                <div className="col-3" />
            </div>
        </>
    );
}

export default StartChatPage;
