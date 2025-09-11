const express = require("express");
const mongoose = require("mongoose");
const bodyParser = require("body-parser");
const cors = require("cors");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const multer = require("multer");

// Config
const app = express();
const PORT = process.env.PORT || 5000;
const JWT_SECRET = "secret123"; // ❗ đổi sang biến môi trường khi deploy

// Middleware
app.use(bodyParser.json());
app.use(cors());

// Multer setup (for file upload)
const storage = multer.memoryStorage();
const upload = multer({ storage });

// MongoDB connect
mongoose.connect("mongodb+srv://<USERNAME>:<PASSWORD>@cluster0.mongodb.net/baidoxe", {
  useNewUrlParser: true,
  useUnifiedTopology: true,
});

// User schema
const UserSchema = new mongoose.Schema({
  username: { type: String, unique: true },
  password: String,
  avatar: String, // store avatar filename or Base64
});

const User = mongoose.model("User", UserSchema);

// Register API
app.post("/api/register", async (req, res) => {
  const { username, password } = req.body;

  const hashedPassword = await bcrypt.hash(password, 10);

  try {
    const user = await User.create({
      username,
      password: hashedPassword,
    });
    res.json({ status: "ok", user });
  } catch (err) {
    res.json({ status: "error", error: "Duplicate username" });
  }
});

// Login API
app.post("/api/login", async (req, res) => {
  const { username, password } = req.body;

  const user = await User.findOne({ username });
  if (!user) {
    return res.json({ status: "error", error: "Invalid login" });
  }

  const isPasswordValid = await bcrypt.compare(password, user.password);
  if (isPasswordValid) {
    const token = jwt.sign({ username: user.username }, JWT_SECRET);
    return res.json({ status: "ok", token });
  } else {
    return res.json({ status: "error", error: "Invalid login" });
  }
});

// Upload avatar API
app.post("/api/updateAvatar", upload.single("avatarFile"), async (req, res) => {
  try {
    const { username } = req.body;
    const avatarFile = req.file;

    if (!avatarFile) {
      return res.status(400).json({ status: "error", error: "No file uploaded" });
    }

    // Save avatar as Base64 in DB (simple way, you can save to storage/cloud instead)
    const avatarBase64 = avatarFile.buffer.toString("base64");

    const user = await User.findOneAndUpdate(
      { username },
      { avatar: avatarBase64 },
      { new: true }
    );

    if (!user) {
      return res.status(404).json({ status: "error", error: "User not found" });
    }

    res.json({ status: "ok", avatar: user.avatar });
  } catch (err) {
    console.error(err);
    res.status(500).json({ status: "error", error: "Server error" });
  }
});

// Run server
app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
