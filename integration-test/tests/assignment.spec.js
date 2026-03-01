import {test, expect} from "@playwright/test";

const ORG_ID = 5;

let authHeaders;

test.beforeAll(() => {
    const token = process.env.E2E_TOKEN;
    if (!token)
        throw new Error("Missing E2E_TOKEN env var");
    authHeaders = {
        Authorization: `Bearer ${token}`,
    };
});

test("Proxy blocks unauthenticated request", async ({request}) => {
    const res = await request.get("/api/v1/organisations");
    expect([401, 403]).toContain(res.status());
});

test("Invalid token is rejected", async ({request}) => {
    const res = await request.get(`/api/v1/organisations`, {
        headers: {Authorization: "Bearer invalid.token.value"},
    });

    expect([401, 403]).toContain(res.status());
});

test("Missing token is rejected", async ({ request }) => {
    const res = await request.get(`/api/v1/user/me`);
    expect([401, 403]).toContain(res.status());
});

test("proxy allows authenticated request", async ({request}) => {
    const token = process.env.E2E_TOKEN;
    if (!token) throw new Error("Missing E2E_TOKEN env var");

    const res = await request.get("/api/v1/organisations", {
        headers: authHeaders,
    });

    expect(res.status()).toBe(200);
});


test("User is created or returned via /user/me", async ({request}) => {
    const token = process.env.E2E_TOKEN;
    if (!token) throw new Error("Missing E2E_TOKEN env var");
    const res = await request.get(`/api/v1/user/me`, {
        headers: authHeaders,
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.organisations).toBeDefined();
});

test("User is added to an organisation.", async ({request}) => {
    const token = process.env.E2E_TOKEN;
    if (!token) throw new Error("Missing E2E_TOKEN env var");
    const res = await request.post(`/api/v1/organisations/assign`, {
        headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
        },
        data: {organisationIds: [5]},
    });
    expect(res.status()).toBe(200);
});

test("user can get credentials with client secret starred", async ({request}) => {
    const token = process.env.E2E_TOKEN;
    if (!token) throw new Error("Missing E2E_TOKEN env var");

    const res = await request.get(
        `/api/v1/credentials/organisations/${1}`,
        {headers: authHeaders,}
    );

    expect(res.status()).toBe(200);
    const body = await res.json();

    expect(body.clientId).toBeDefined();
    expect(body.clientSecret).toEqual("***");
    expect(body.status).toBe("ACTIVE");
});


test("user can create credentials", async ({request}) => {
    const token = process.env.E2E_TOKEN;
    if (!token) throw new Error("Missing E2E_TOKEN env var");

    const res = await request.post(
        `/api/v1/credentials/organisations/${ORG_ID}`,
        {headers: authHeaders,}
    );

    expect(res.status()).toBe(200);
    const body = await res.json();

    expect(body.clientId).toBeDefined();
    expect(body.clientSecret).toBeDefined();
    expect(body.status).toBe("ACTIVE");
});

test("Cannot create credentials if user not member of org", async ({request}) => {
    const res = await request.post(
        `/api/v1/credentials/organisations/9999`,
        {headers: authHeaders}
    );

    expect([400, 403]).toContain(res.status());
});

test("cannot create credentials with expiration > 90 days", async ({request}) => {
    const future = new Date();
    future.setDate(future.getDate() + 120);

    const res = await request.post(
        `/api/v1/credentials/organisations/5`,
        {
            headers: {
                ...authHeaders,
                "Content-Type": "application/json",
            },
            data: {
                expiresAt: future.toISOString(),
            },
        }
    );

    expect([400, 422]).toContain(res.status());
});

test("user can update/rotate credentials secret", async ({request}) => {
    const token = process.env.E2E_TOKEN;
    if (!token) throw new Error("Missing E2E_TOKEN env var");

    const res = await request.put(
        `/api/v1/credentials/organisations/${ORG_ID}`,
        {headers: authHeaders,}
    );
    const resUpdated = await request.put(
        `/api/v1/credentials/organisations/${ORG_ID}`,
        {headers: authHeaders,}
    );

    const body = await res.json();
    const bodyUpdated = await resUpdated.json();

    expect(res.status()).toBe(200);
    expect(resUpdated.status()).toBe(200);
    expect(body.clientSecret).not.toEqual(bodyUpdated.clientSecret);

});

test("user can delete credentials", async ({request}) => {
    const token = process.env.E2E_TOKEN;
    if (!token) throw new Error("Missing E2E_TOKEN env var");

    const res = await request.delete(
        `/api/v1/credentials/organisations/${ORG_ID}`,
        {headers: authHeaders,}
    );
    expect([200, 204]).toContain(res.status());

});

test("deleted credentials no longer work", async ({request}) => {
    const token = process.env.E2E_TOKEN;
    if (!token) throw new Error("Missing E2E_TOKEN env var");

    const res = await request.get(
        `/api/v1/credentials/${ORG_ID}`,
        {headers: authHeaders,}
    );

    expect([404, 400]).toContain(res.status());
});

