function Home() {
    return (
        <div>
            <div className="text-center py-4 mb-4">
                <i className="bi bi-chat-dots-fill display-4 text-primary" aria-hidden="true" />
                <h1 className="mt-3">Live Chat Support</h1>
                <p className="lead text-muted">
                    Running into a hardware or software problem? Start a chat and tell us
                    what&apos;s going on.
                </p>
            </div>

            <div className="row g-4">
                <div className="col-md-4 d-flex">
                    <div className="hero-icon bg-primary-subtle text-primary me-3">
                        <i className="bi bi-chat-text" aria-hidden="true" />
                    </div>
                    <div>
                        <h6 className="mb-1">Start a chat</h6>
                        <p className="text-muted mb-0">
                            Tell us a bit about the problem and we&apos;ll get you into a chat
                            room right away.
                        </p>
                    </div>
                </div>

                <div className="col-md-4 d-flex">
                    <div className="hero-icon bg-primary-subtle text-primary me-3">
                        <i className="bi bi-headset" aria-hidden="true" />
                    </div>
                    <div>
                        <h6 className="mb-1">Talk to an agent</h6>
                        <p className="text-muted mb-0">
                            A support agent joins your chat and works with you in real time
                            until it&apos;s solved.
                        </p>
                    </div>
                </div>

                <div className="col-md-4 d-flex">
                    <div className="hero-icon bg-primary-subtle text-primary me-3">
                        <i className="bi bi-check-circle" aria-hidden="true" />
                    </div>
                    <div>
                        <h6 className="mb-1">Stick around until it's solved</h6>
                        <p className="text-muted mb-0">
                            Leaving the chat early won&apos;t solve your problem. Wait for the
                            agent to close it out once things are fixed.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Home;
