import { json } from '@sveltejs/kit';
import { proxyPost } from '$lib/apiProxy.js';
import { setAuthToken } from '$lib/auth.js';

export async function handleAuthProxyPost(event,targetPath,
  {
    setCookie = true,
    sendAuth = false,
    errorLabel = 'Auth'
  } = {}
) {
  try {
    const resp = await proxyPost(event, targetPath, sendAuth);
    const status = resp.status;
    const contentType = resp.headers.get('content-type') ?? 'text/plain';
    const raw = await resp.text();

    let payload = null;
        if (raw !== null && raw !== undefined) {
      try {
        payload = JSON.parse(raw);
      } catch {
        payload = null;
      }
    }

    const token = payload?.data;
    if (setCookie && token) {
      setAuthToken(event.cookies, token);
    }

    if (payload) {
      return json(payload, { status });
    }

    return new Response(raw, {
      status,
      headers: { 'content-type': contentType }
    });
  } catch (err) {
    console.error(`${errorLabel} error:`, err);
    return json(
      { success: false, message: `Internal error occurred while ${errorLabel} ` },
      { status: 500 }
    );
  }
}