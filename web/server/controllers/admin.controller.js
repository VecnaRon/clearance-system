//admin.controller.js
import * as adminService from "../services/admin.service.js";

export async function overview(req, res) {
  try {
    res.json(await adminService.getOverview());
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Failed to load overview" });
  }
}

export async function departments(req, res) {
  try {
    if (req.method === "GET") return res.json(await adminService.listDepartments());
    if (req.method === "POST") {
      return res.status(201).json(await adminService.createDepartment(req.body, req.user.email));
    }
    if (req.method === "PUT") {
      return res.json(await adminService.updateDepartment(req.params.id, req.body, req.user.email));
    }
    if (req.method === "DELETE") {
      return res.json(await adminService.deleteDepartment(req.params.id, req.user.email));
    }
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Department operation failed" });
  }
}

export async function users(req, res) {
  try {
    if (req.method === "GET") {
      return res.json(await adminService.listUsers(req.query));
    }
    if (req.method === "POST") {
      return res.status(201).json(await adminService.createUser(req.body, req.user.email));
    }
    if (req.method === "PUT") {
      return res.json(await adminService.updateUser(req.params.id, req.body, req.user.email));
    }
    if (req.method === "DELETE") {
      return res.json(await adminService.deleteUser(req.params.id, req.user.email));
    }
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "User operation failed" });
  }
}

export async function createUserAdmin(req, res) {
  try {
    res.status(201).json(await adminService.createUser(req.body, req.user.email));
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Failed to create user" });
  }
}

export async function getPendingUsers(req, res) {
  try {
    res.json(await adminService.getPendingUsers());
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Failed to load pending users" });
  }
}

export async function activateUser(req, res) {
  try {
    res.json(await adminService.activateUser(req.params.id, req.user.email));
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Failed to activate user" });
  }
}

export async function deactivateUser(req, res) {
  try {
    res.json(await adminService.deactivateUser(req.params.id, req.user.email));
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Failed to deactivate user" });
  }
}

export async function auditLogs(req, res) {
  try {
    res.json(await adminService.getAuditLogs());
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Failed to load audit logs" });
  }
}

export async function report(req, res) {
  try {
    res.json(await adminService.reportClearanceSummary());
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Failed to load report" });
  }
}

// Deprecated — old Node schema helpers, no Spring equivalent, tables no longer exist.
// Kept as stubs (not deleted) so routes.js doesn't break on import.
export async function fixClearanceRequests(req, res) {
  res.status(410).json({ message: "Deprecated: no longer applicable to the current schema" });
}
export async function fixAwaitingFinal(req, res) {
  res.status(410).json({ message: "Deprecated: no longer applicable to the current schema" });
}