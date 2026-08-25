// ✅ Replace entire storage.js with this

const TOKEN_KEY = "token"
const USER_KEY  = "user"

// sessionStorage clears automatically when browser/tab is closed
// This prevents someone else from accessing the session later
export const saveToken = (token) =>
    sessionStorage.setItem(TOKEN_KEY, token)

export const getToken = () =>
    sessionStorage.getItem(TOKEN_KEY)

export const clearToken = () =>
    sessionStorage.removeItem(TOKEN_KEY)

export const saveUser = (u) =>
    sessionStorage.setItem(USER_KEY, JSON.stringify(u))

export const getUser = () => {
    const u = sessionStorage.getItem(USER_KEY)
    return u ? JSON.parse(u) : null
}

export const clearUser = () =>
    sessionStorage.removeItem(USER_KEY)

// Clears everything — call this on logout
export const clearAll = () => {
    sessionStorage.removeItem(TOKEN_KEY)
    sessionStorage.removeItem(USER_KEY)
}
//getting user's role.
export const getRole = () => {
    const user = getUser()
    return user?.role || null   // returns e.g. "student", "staff", "hod", "admin"
}