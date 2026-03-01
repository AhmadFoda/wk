// App.jsx (FULL FRONTEND SOLUTION - minimal changes, adds /credentials page)
// Requires: npm i react-router-dom @react-oauth/google
import {GoogleLogin} from "@react-oauth/google";
import {useEffect, useMemo, useState} from "react";
import {BrowserRouter, Routes, Route, useNavigate} from "react-router-dom";

const API = "http://localhost:8080";

/** -------- Shared small helpers -------- */
async function fetchTextSafe(res) {
    try {
        return await res.text();
    } catch {
        return "";
    }
}

async function fetchJson(url, options) {
    const res = await fetch(url, options);
    if (!res.ok) {
        const text = await fetchTextSafe(res);
        throw new Error(`${res.status} ${res.statusText} - ${text}`);
    }
    // some endpoints might return empty body
    const ct = res.headers.get("content-type") || "";
    if (!ct.includes("application/json")) return null;
    return res.json();
}

/** -------- Root wrapper keeps token and routes -------- */
export default function App() {
    const [token, setToken] = useState(null);

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/session-org" element={<SessionOrgPage token={token}/>}/>
                <Route path="/" element={<OrganisationsPage token={token} setToken={setToken}/>}/>
                <Route path="/credentials/select" element={<SelectOrgForCredentials token={token}/>}/>
                <Route path="/credentials" element={<CredentialsPage token={token}/>}/>
            </Routes>
        </BrowserRouter>
    );
}

function SessionOrgPage({token}) {
    const navigate = useNavigate();
    const [allOrgs, setAllOrgs] = useState([]);
    const [myOrgs, setMyOrgs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (loading) return <p style={{padding: 40}}>Loading...</p>;
        if (!token) return;
        let cancelled = false;

        const run = async () => {
            setLoading(true);
            setError(null);
            try {
                const headers = {Authorization: `Bearer ${token}`};

                await fetch(`${API}/api/v1/user/me`, {headers});

                const [all, mine] = await Promise.all([
                    fetchJson(`${API}/api/v1/organisations`, {headers}),
                    fetchJson(`${API}/api/v1/user/organisations`, {headers}),
                ]);

                if (cancelled) return;
                setAllOrgs(all || []);
                const mineList = mine || [];

                if (!cancelled && mineList.length === 1) {
                    sessionStorage.setItem("activeOrgId", String(mineList[0].id));
                    navigate("/credentials", {replace: true});
                    return; // stop rendering the chooser
                }

                setAllOrgs(all || []);
                setMyOrgs(mineList);
            } catch (e) {
                if (!cancelled) setError(e.message || "Failed to load organisations");
            } finally {
                if (!cancelled) setLoading(false);
            }
        };

        run();
        return () => {
            cancelled = true;
        };
    }, [token]);

    if (!token) {
        return (
            <div style={{padding: 40, maxWidth: 700, margin: "0 auto", fontFamily: "Arial"}}>
                <h2>Choose organisation</h2>
                <p>Please login first.</p>
                <button onClick={() => navigate("/")} style={btn()}>Back</button>
            </div>
        );
    }

    const hasAny = myOrgs.length > 0;
    const listToShow = hasAny ? myOrgs : allOrgs;

    const choose = async (orgId) => {
        try {
            setError(null);
            const headers = {Authorization: `Bearer ${token}`, "Content-Type": "application/json"};

            // If user has no orgs yet, link them to the chosen org first
            if (!hasAny) {
                await fetchJson(`${API}/api/v1/organisations/assign`, {
                    method: "POST",
                    headers,
                    body: JSON.stringify({organisationIds: [orgId]}),
                });
            }

            // Set session org and go to credentials
            sessionStorage.setItem("activeOrgId", String(orgId));
            navigate("/credentials", {replace: true});
        } catch (e) {
            setError(e.message || "Failed to select organisation");
        }
    };

    return (
        <div style={{padding: 40, maxWidth: 700, margin: "0 auto", fontFamily: "Arial"}}>
            <h2 style={{marginTop: 0}}>
                {hasAny ? "Choose organisation to manage credentials" : "Pick your first organisation"}
            </h2>

            <div style={{marginTop: 8, color: "#555"}}>
                {hasAny
                    ? "You can belong to many organisations, but you must pick one for this credentials session."
                    : "You are not linked to any organisation yet. Pick one to join, then continue."}
            </div>

            {error && (
                <div style={{
                    marginTop: 16,
                    padding: "10px 12px",
                    border: "1px solid #f5c2c7",
                    background: "#f8d7da",
                    borderRadius: 6
                }}>
                    <strong>Error:</strong> {error}
                </div>
            )}

            {loading ? (
                <p style={{marginTop: 16}}>Loading...</p>
            ) : (
                <div style={{marginTop: 16}}>
                    {listToShow.map((org) => (
                        <div key={org.id} style={{
                            display: "flex", justifyContent: "space-between", alignItems: "center",
                            padding: "12px 16px", marginBottom: 10, border: "1px solid #ddd", borderRadius: 6
                        }}>
                            <span style={{fontWeight: 500}}>{org.name}</span>
                            <button
                                onClick={() => choose(org.id)}
                                style={{
                                    padding: "6px 14px",
                                    borderRadius: 4,
                                    border: "none",
                                    cursor: "pointer",
                                    background: "#0d6efd",
                                    color: "#fff"
                                }}
                            >
                                Select
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

function OrganisationsPage({token, setToken}) {
    const navigate = useNavigate();

    const [orgs, setOrgs] = useState([]);
    const [selected, setSelected] = useState([]);
    const [loading, setLoading] = useState(false);
    const [busyId] = useState(null);
    const [error, setError] = useState(null);
    const [showToken, setShowToken] = useState(false);
    const [loadedMine, setLoadingMine] = useState(false);
    const [initialSelected, setInitialSelected] = useState([]);

    useEffect(() => {
        if (token) navigate("/session-org", {replace: true});
    }, [token, navigate]);

    useEffect(() => {
        if (!token) return;
        if (!loadedMine) return;

        // If already chosen for session, go straight to credentials
        const active = sessionStorage.getItem("activeOrgId");
        if (active) {
            navigate("/credentials", {replace: true});
            return;
        }

        // If user has memberships already, force session org choice
        if (selected.length === 1) {
            sessionStorage.setItem("activeOrgId", String(selected[0]));
            navigate("/credentials", {replace: true});
        } else if (selected.length > 1) {
            navigate("/credentials/select", {replace: true});
        }
    }, [token, loadedMine, selected, navigate]);

    useEffect(() => {
        if (!token) return;

        let cancelled = false;

        const fetchData = async () => {
            try {
                setError(null);
                setLoading(true);

                const headers = {Authorization: `Bearer ${token}`};

                // creates user if doesn't exist (your backend behavior)
                await fetch(`${API}/api/v1/user/me`, {headers});

                const [all, mine] = await Promise.all([
                    fetchJson(`${API}/api/v1/organisations`, {headers}),
                    fetchJson(`${API}/api/v1/user/organisations`, {headers}),
                ]);

                if (cancelled) return;

                setOrgs(all || []);
                const mineIds = (mine || []).map((o) => o.id);
                setSelected(mineIds);
                setInitialSelected(mineIds);
                setLoadingMine(true);
            } catch (e) {
                if (!cancelled) setError(e.message || "Failed to load data");
            } finally {
                if (!cancelled) setLoading(false);
            }
        };


        fetchData();
        return () => {
            cancelled = true;
        };
    }, [token]);
    const saveMemberships = async () => {
        if (!token) return;

        // validate
        if (selected.length === 0) {
            setError("Select at least one organisation, then click Save.");
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const headersAuth = {Authorization: `Bearer ${token}`};
            const headersJson = {...headersAuth, "Content-Type": "application/json"};

            const toAdd = selected.filter((id) => !initialSelected.includes(id));
            const toRemove = initialSelected.filter((id) => !selected.includes(id));

            // add in one POST (your backend supports array)
            if (toAdd.length > 0) {
                await fetchJson(`${API}/api/v1/organisations/assign`, {
                    method: "POST",
                    headers: headersJson,
                    body: JSON.stringify({organisationIds: toAdd}),
                });
            }

            // remove via DELETE per id (your backend supports this)
            for (const id of toRemove) {
                const res = await fetch(`${API}/api/v1/user/organisations/${id}`, {
                    method: "DELETE",
                    headers: headersAuth,
                });
                if (!res.ok) throw new Error(`DELETE failed: ${res.status} ${res.statusText}`);
            }

            // update baseline
            setInitialSelected(selected);

            // now decide session org routing
            if (selected.length === 1) {
                sessionStorage.setItem("activeOrgId", String(selected[0]));
                navigate("/credentials");
            } else {
                navigate("/credentials/select");
            }
        } catch (e) {
            setError(e.message || "Save failed");
        } finally {
            setLoading(false);
        }
    };
    const toggle = (id) => {
        setSelected((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
    };

    const selectionHint = useMemo(() => {
        if (!token) return "";
        if (loading) return "";
        if (orgs.length === 0) return "No organisations available.";
        return "Select the organisations you belong to. Managing credentials is chosen on the next page (it won’t unlink anything).";
    }, [token, loading, orgs.length]);
    return (
        <div style={{padding: 40, maxWidth: 700, margin: "0 auto", fontFamily: "Arial"}}>
            <h2 style={{marginBottom: 20}}>Credentials Registration</h2>

            {!token ? (
                <GoogleLogin
                    onSuccess={(r) => {
                        setToken(r.credential);
                        // let React set token first; navigate will happen in an effect
                    }}
                    onError={() => console.log("Login Failed")}
                />
            ) : (
                <>
                    <div style={{marginBottom: 20, display: "flex", gap: 10, alignItems: "center", flexWrap: "wrap"}}>
                        <button
                            onClick={() => setToken(null)}
                            style={{
                                padding: "6px 12px",
                                borderRadius: 4,
                                border: "1px solid #ccc",
                                background: "#f5f5f5",
                                cursor: "pointer",
                            }}
                        >
                            Logout
                        </button>

                        <button
                            onClick={() => setShowToken((v) => !v)}
                            style={{
                                padding: "6px 12px",
                                borderRadius: 4,
                                border: "1px solid #ccc",
                                background: "#f5f5f5",
                                cursor: "pointer",
                            }}
                        >
                            {showToken ? "Hide Token" : "Show Token (Debug)"}
                        </button>
                        <button
                            onClick={() => {
                                if (selected.length === 0) {
                                    setError("Select at least one organisation first.");
                                    return;
                                }

                                if (selected.length === 1) {
                                    sessionStorage.setItem("activeOrgId", String(selected[0]));
                                    navigate("/credentials");
                                    return;
                                }

                                // 2+ orgs: must choose one for the credentials session
                                navigate("/credentials/select");
                            }}
                            style={{
                                padding: "6px 12px",
                                borderRadius: 4,
                                border: "1px solid #0d6efd",
                                background: "#0d6efd",
                                color: "#fff",
                                cursor: "pointer",
                            }}
                        >
                            Manage credentials
                        </button>
                        <button
                            onClick={saveMemberships}
                            disabled={loading}
                            style={{
                                padding: "6px 12px",
                                borderRadius: 4,
                                border: "1px solid #198754",
                                background: "#198754",
                                color: "#fff",
                                cursor: loading ? "not-allowed" : "pointer",
                            }}
                        >
                            {loading ? "Saving..." : "Save"}
                        </button>
                    </div>

                    {/* NEW: helper text for the assignment constraint */}
                    <div style={{marginBottom: 12, fontSize: 13, color: "#555"}}>{selectionHint}</div>

                    {showToken && (
                        <textarea style={{width: "100%", height: 120, marginBottom: 16, fontSize: 12}} readOnly
                                  value={token}/>
                    )}

                    {error && (
                        <div
                            style={{
                                marginBottom: 16,
                                padding: "10px 12px",
                                border: "1px solid #f5c2c7",
                                background: "#f8d7da",
                                borderRadius: 6,
                            }}
                        >
                            <strong>Error:</strong> {error}
                        </div>
                    )}

                    {loading ? (
                        <p>Loading organisations...</p>
                    ) : (
                        orgs.map((org) => {
                            const isSelected = selected.includes(org.id);
                            const isBusy = busyId === org.id;

                            return (
                                <div
                                    key={org.id}
                                    style={{
                                        display: "flex",
                                        justifyContent: "space-between",
                                        alignItems: "center",
                                        padding: "12px 16px",
                                        marginBottom: 10,
                                        border: "1px solid #ddd",
                                        borderRadius: 6,
                                        background: isSelected ? "#eef6ff" : "#ffffff",
                                    }}
                                >
                                    <span style={{fontWeight: 500}}>{org.name}</span>

                                    <button
                                        disabled={isBusy}
                                        onClick={() => toggle(org.id)}
                                        style={{
                                            padding: "6px 14px",
                                            borderRadius: 4,
                                            border: "none",
                                            cursor: isBusy ? "not-allowed" : "pointer",
                                            background: isSelected ? "#dc3545" : "#0d6efd",
                                            color: "#fff",
                                            opacity: isBusy ? 0.7 : 1,
                                        }}
                                    >
                                        {isBusy ? "..." : isSelected ? "Unselect" : "Select"}
                                    </button>
                                </div>
                            );
                        })
                    )}
                </>
            )}
        </div>
    );
}

/** -------- Page 2: Credentials operations for one org --------
 * You must map these endpoints to your backend:
 * - GET    /api/v1/credentials/organisations/{orgId}          (returns active credentials or 404/empty)
 * - POST   /api/v1/credentials/organisations/{orgId}          (create new if none active)
 * - PUT    /api/v1/credentials/organisations/{orgId}          (rotate/update)
 * - DELETE /api/v1/credentials/organisations/{orgId}          (revoke)
 */
function CredentialsPage({token}) {
    const navigate = useNavigate();
    const orgId = sessionStorage.getItem("activeOrgId");

    const [loading, setLoading] = useState(false);
    const [working, setWorking] = useState(false);
    const [error, setError] = useState(null);

    const [cred, setCred] = useState(null); // {id, clientId, clientSecret, expirationDate, status}
    const [expiresAt, setExpiresAt] = useState(""); // optional input (datetime-local)

    // simple datetime-local -> ISO
    const expiresAtIso = useMemo(() => {
        if (!expiresAt) return null;
        const d = new Date(expiresAt);
        if (Number.isNaN(d.getTime())) return null;
        return d.toISOString();
    }, [expiresAt]);

    useEffect(() => {
        if (!token) return;
        if (!orgId) return;

        let cancelled = false;

        const load = async () => {
            setError(null);
            setLoading(true);
            try {
                const headers = {Authorization: `Bearer ${token}`};

                // If your backend returns 404 when no credentials, handle it gracefully.
                const res = await fetch(`${API}/api/v1/credentials/organisations/${orgId}`, {headers});

                if (res.status === 404) {
                    if (!cancelled) setCred(null);
                    return;
                }

                if (!res.ok) {
                    const text = await fetchTextSafe(res);
                    throw new Error(`${res.status} ${res.statusText} - ${text}`);
                }

                const ct = res.headers.get("content-type") || "";
                const data = ct.includes("application/json") ? await res.json() : null;

                if (!cancelled) setCred(data);
            } catch (e) {
                if (!cancelled) setError(e.message || "Failed to load credentials");
            } finally {
                if (!cancelled) setLoading(false);
            }
        };

        load();
        return () => {
            cancelled = true;
        };
    }, [token, orgId]);

    if (!token) {
        return (
            <div style={{padding: 40, maxWidth: 700, margin: "0 auto", fontFamily: "Arial"}}>
                <h2>Credentials</h2>
                <p>You are not logged in. Go back and login first.</p>
                <button onClick={() => navigate("/")} style={btn()}>
                    Back
                </button>
            </div>
        );
    }

    if (!orgId) {
        return (
            <div style={{padding: 40, maxWidth: 700, margin: "0 auto", fontFamily: "Arial"}}>
                <h2>Credentials</h2>
                <p>Missing orgId. Go back and choose an organisation</p>
                <button onClick={() => navigate("/")} style={btn()}>
                    Back
                </button>
            </div>
        );
    }

    const headers = {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
    };

    const refresh = async () => {
        // re-run the GET logic quickly
        setError(null);
        setLoading(true);
        try {
            const res = await fetch(`${API}/api/v1/credentials/organisations/${orgId}`, {
                headers: {Authorization: `Bearer ${token}`},
            });

            if (res.status === 404) {
                setCred(null);
                return;
            }
            if (!res.ok) {
                const text = await fetchTextSafe(res);
                throw new Error(`${res.status} ${res.statusText} - ${text}`);
            }
            const ct = res.headers.get("content-type") || "";
            const data = ct.includes("application/json") ? await res.json() : null;
            setCred(data);
        } catch (e) {
            setError(e.message || "Failed to load credentials");
        } finally {
            setLoading(false);
        }
    };

    const create = async () => {
        setWorking(true);
        setError(null);
        try {
            const body = expiresAtIso ? {expiresAt: expiresAtIso} : {};
            const data = await fetchJson(`${API}/api/v1/credentials/organisations/${orgId}`, {
                method: "POST",
                headers,
                body: JSON.stringify(body),
            });
            setCred(data);
        } catch (e) {
            setError(e.message || "Create failed");
        } finally {
            setWorking(false);
        }
    };

    const rotate = async () => {
        setWorking(true);
        setError(null);
        try {
            const body = expiresAtIso ? {expiresAt: expiresAtIso} : {};
            const data = await fetchJson(`${API}/api/v1/credentials/organisations/${orgId}`, {
                method: "PUT",
                headers,
                body: JSON.stringify(body),
            });
            setCred(data);
        } catch (e) {
            setError(e.message || "Update failed");
        } finally {
            setWorking(false);
        }
    };

    const revoke = async () => {
        setWorking(true);
        setError(null);
        try {
            const res = await fetch(`${API}/api/v1/credentials/organisations/${orgId}`, {
                method: "DELETE",
                headers: {Authorization: `Bearer ${token}`},
            });
            if (!res.ok) {
                const text = await fetchTextSafe(res);
                throw new Error(`${res.status} ${res.statusText} - ${text}`);
            }
            setCred(null);
        } catch (e) {
            setError(e.message || "Delete failed");
        } finally {
            setWorking(false);
        }
    };

    const hasActive = cred && String(cred.status || "").toUpperCase() === "ACTIVE";

    return (
        <div style={{padding: 40, maxWidth: 700, margin: "0 auto", fontFamily: "Arial"}}>
            <div style={{display: "flex", justifyContent: "space-between", alignItems: "center", gap: 10}}>
                <h2 style={{margin: 0}}>Credentials</h2>
                <button onClick={() => navigate("/")} style={btn()}>
                    Back
                </button>
            </div>

            <div style={{marginTop: 8, color: "#555"}}>
                Managing credentials for organisation <b>#{orgId}</b>
            </div>

            {error && (
                <div
                    style={{
                        marginTop: 16,
                        marginBottom: 16,
                        padding: "10px 12px",
                        border: "1px solid #f5c2c7",
                        background: "#f8d7da",
                        borderRadius: 6,
                    }}
                >
                    <strong>Error:</strong> {error}
                </div>
            )}

            <div style={{marginTop: 16, display: "flex", gap: 10, flexWrap: "wrap", alignItems: "center"}}>
                <label style={{fontSize: 13, color: "#333"}}>
                    Expires at (optional):{" "}
                    <input
                        type="datetime-local"
                        value={expiresAt}
                        onChange={(e) => setExpiresAt(e.target.value)}
                        style={{
                            marginLeft: 8,
                            padding: "6px 8px",
                            borderRadius: 4,
                            border: "1px solid #ccc",
                        }}
                    />
                </label>

                <button disabled={loading || working} onClick={refresh} style={btn()}>
                    Refresh
                </button>
            </div>

            <div style={{marginTop: 18, padding: 16, border: "1px solid #ddd", borderRadius: 8}}>
                <div style={{marginBottom: 10, fontWeight: 600}}>Current state</div>

                {loading ? (
                    <div>Loading...</div>
                ) : !cred ? (
                    <div style={{color: "#555"}}>No ACTIVE credentials found for this organisation.</div>
                ) : (
                    <div style={{display: "grid", gap: 8, fontSize: 14}}>
                        <Row label="Status" value={String(cred.status)}/>
                        <Row label="Credential ID" value={String(cred.id ?? "")}/>
                        <Row label="Client ID" value={String(cred.clientId ?? "")}/>
                        <Row label="Client Secret" value={String(cred.clientSecret ?? "")}/>
                        <Row label="Expires At" value={String(cred.expirationDate ?? "")}/>
                    </div>
                )}
            </div>

            <div style={{marginTop: 16, display: "flex", gap: 10, flexWrap: "wrap"}}>
                <button
                    disabled={working || loading || !!cred} // create only when none active
                    onClick={create}
                    style={{
                        ...btnPrimary(),
                        cursor: working || loading || cred ? "not-allowed" : "pointer",
                    }}
                    title={cred ? "Already have credentials (rotate instead)" : "Create new credentials"}
                >
                    Create
                </button>

                <button
                    disabled={working || loading || !hasActive}
                    onClick={rotate}
                    style={{
                        ...btnPrimary(),
                        background: hasActive ? "#198754" : "#d1e7dd",
                        cursor: working || loading || !hasActive ? "not-allowed" : "pointer",
                    }}
                    title={!hasActive ? "No ACTIVE credentials to rotate" : "Rotate credentials"}
                >
                    Rotate / Update
                </button>

                <button
                    disabled={working || loading || !cred}
                    onClick={revoke}
                    style={{
                        ...btnPrimary(),
                        background: cred ? "#dc3545" : "#f8d7da",
                        cursor: working || loading || !cred ? "not-allowed" : "pointer",
                    }}
                    title={!cred ? "Nothing to revoke" : "Revoke credentials"}
                >
                    Revoke / Delete
                </button>
            </div>

            <div style={{marginTop: 14, fontSize: 12, color: "#666"}}>
                Tip: client secret is shown only when backend returns it. After refresh, your backend might choose not
                to return it
                again (that’s fine).
            </div>
        </div>
    );
}

/** -------- tiny UI helpers -------- */
function Row({label, value}) {
    return (
        <div style={{display: "grid", gridTemplateColumns: "140px 1fr", gap: 10}}>
            <div style={{color: "#555"}}>{label}</div>
            <div style={{fontFamily: "monospace", wordBreak: "break-all"}}>{value}</div>
        </div>
    );
}

function btn() {
    return {
        padding: "6px 12px",
        borderRadius: 4,
        border: "1px solid #ccc",
        background: "#f5f5f5",
        cursor: "pointer",
    };
}

function btnPrimary() {
    return {
        padding: "8px 14px",
        borderRadius: 6,
        border: "none",
        color: "#fff",
        fontWeight: 600,
    };
}

function SelectOrgForCredentials({token}) {
    const navigate = useNavigate();

    const [orgs, setOrgs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!token) return;

        let cancelled = false;

        const load = async () => {
            setLoading(true);
            setError(null);
            try {
                const headers = {Authorization: `Bearer ${token}`};

                const mine = await fetchJson(`${API}/api/v1/user/organisations`, {headers});
                const list = mine || [];
                if (cancelled) return;
                setOrgs(list);

                if (list.length === 1) {
                    sessionStorage.setItem("activeOrgId", String(list[0].id));
                    navigate("/credentials", {replace: true});
                }
            } catch (e) {
                if (!cancelled) setError(e.message || "Failed to load your organisations");
            } finally {
                if (!cancelled) setLoading(false);
            }
        };

        load();
        return () => {
            cancelled = true;
        };
    }, [token]);

    if (!token) {
        return (
            <div style={{padding: 40, maxWidth: 700, margin: "0 auto", fontFamily: "Arial"}}>
                <h2>Select organisation</h2>
                <p>You are not logged in. Go back and login first.</p>
                <button onClick={() => navigate("/")} style={btn()}>
                    Back
                </button>
            </div>
        );
    }

    return (
        <div style={{padding: 40, maxWidth: 700, margin: "0 auto", fontFamily: "Arial"}}>
            <div style={{display: "flex", justifyContent: "space-between", alignItems: "center", gap: 10}}>
                <h2 style={{margin: 0}}>Select organisation</h2>
                <button onClick={() => navigate("/")} style={btn()}>
                    Back
                </button>
            </div>

            <div style={{marginTop: 8, color: "#555"}}>
                Choose <b>one</b> organisation to manage credentials for. This does <b>not</b> change your memberships.
            </div>

            {error && (
                <div
                    style={{
                        marginTop: 16,
                        marginBottom: 16,
                        padding: "10px 12px",
                        border: "1px solid #f5c2c7",
                        background: "#f8d7da",
                        borderRadius: 6,
                    }}
                >
                    <strong>Error:</strong> {error}
                </div>
            )}

            {loading ? (
                <p style={{marginTop: 16}}>Loading your organisations...</p>
            ) : orgs.length === 0 ? (
                <div style={{marginTop: 16, color: "#555"}}>
                    You are not linked to any organisations yet. Go back and select at least one.
                </div>
            ) : (
                <div style={{marginTop: 16}}>
                    {orgs.map((org) => (
                        <div
                            key={org.id}
                            style={{
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center",
                                padding: "12px 16px",
                                marginBottom: 10,
                                border: "1px solid #ddd",
                                borderRadius: 6,
                                background: "#ffffff",
                            }}
                        >
                            <span style={{fontWeight: 500}}>{org.name}</span>
                            <button
                                onClick={() => {
                                    sessionStorage.setItem("activeOrgId", String(org.id));
                                    navigate(`/credentials`)
                                }}
                                style={{
                                    padding: "6px 14px",
                                    borderRadius: 4,
                                    border: "none",
                                    cursor: "pointer",
                                    background: "#0d6efd",
                                    color: "#fff",
                                }}
                            >
                                Manage
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}