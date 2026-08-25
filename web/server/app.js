import express from "express"
import cors from "cors"
import helmet from "helmet"
import { config } from "dotenv"
import authRoutes from "./routes/auth.routes.js"
import userRoutes from "./routes/user.routes.js"
import studentRoutes from "./routes/student.routes.js"
import deptRoutes from "./routes/department.routes.js"
import clearanceRoutes from "./routes/clearance.routes.js"
import adminRoutes from "./routes/admin.routes.js"
import recordsRoutes from "./routes/records.routes.js"


config()

const app = express()

app.use(helmet())
const allowedOrigins = [
    "http://localhost:3000",
    "http://192.168.1.150:3000",
];

app.use(cors({
    origin(origin, callback) {
        // Allow requests without an Origin header (Postman, curl)
        if (!origin) {
            return callback(null, true);
        }

        if (allowedOrigins.includes(origin)) {
            return callback(null, true);
        }

        return callback(new Error(`Origin ${origin} not allowed by CORS`));
    },
    credentials: true,
}));
app.use(express.json())

app.get("/api/health", (req, res) => res.json({ ok: true }))

app.use("/api/auth", authRoutes)
app.use("/api/users", userRoutes)
app.use("/api/student", studentRoutes)
app.use("/api/department", deptRoutes)
app.use("/api/clearance", clearanceRoutes)
app.use("/api/admin", adminRoutes)
app.use("/api/records", recordsRoutes)

app.use((err, req, res, next) => {
  console.error(err)
  res.status(err.status || 500).json({ message: err.message || "Server error" })
})

export default app
